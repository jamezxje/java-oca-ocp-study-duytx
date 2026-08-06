# Cơ Chế Vận Hành Bên Trong (Under The Hood) Của List Và Các Class Triển Khai

Tài liệu này đi sâu vào **mã nguồn (Source Code) và cơ chế bộ nhớ JVM** của tất cả các lớp triển khai `List` trong Java: `ArrayList`, `LinkedList`, `Vector`, `Stack` và `CopyOnWriteArrayList`.

---

## 1. `ArrayList` - Cơ Chế Mã Nguồn Bên Trong

```
+-------------------------------------------------------+
|  ArrayList<E>                                         |
|  private transient Object[] elementData;             |
|  private int size;                                    |
+-------------------------------------------------------+
```

### A. Khởi Tạo Bộ Nhớ Ban Đầu (Lazy Initialization)
* Trong Java 7 trở về trước: Khai báo `new ArrayList<>()` sẽ cấp phát ngay lập tức mảng `Object[10]`.
* **Từ Java 8 trở đi (Tối ưu RAM):** Khai báo `new ArrayList<>()` chỉ tạo một mảng rỗng `DEFAULTCAPACITY_EMPTY_ELEMENTDATA = {}` (size = 0).
* **Thời điểm cấp phát thật:** Chỉ khi bạn gọi câu lệnh `.add()` **đầu tiên**, JVM mới cấp phát mảng có dung lượng mặc định là **10** (`DEFAULT_CAPACITY = 10`).

### B. Thuật Toán Tự Mở Rộng Kích Thước (Capacity Expansion - 1.5x)
Khi gọi `add()`, nếu `size + 1 > elementData.length`:
1. JVM tính dung lượng mới bằng toán tử bít:
   ```java
   int newCapacity = oldCapacity + (oldCapacity >> 1); // Tăng 1.5 lần (10 -> 15 -> 22 -> 33...)
   ```
2. Gọi hàm native của hệ điều hành `Arrays.copyOf(elementData, newCapacity)` (bên dưới dùng `System.arraycopy`) để cấp phát vùng nhớ liên tục mới và chép dữ liệu cũ sang.

### C. Thuật Toán Chèn / Xóa Ở Giữa Mảng
* Khi gọi `remove(index)` hoặc `add(index, element)`:
  ```java
  int numMoved = size - index - 1;
  if (numMoved > 0)
      System.arraycopy(elementData, index + 1, elementData, index, numMoved);
  elementData[--size] = null; // Cực kỳ quan trọng: Gán null để GC thu gom rác!
  ```
* **Đánh giá:** Phải dời toàn bộ `numMoved` phần tử sang trái/phải nên độ phức tạp là **$O(N)$**.

### D. Cơ Chế Fail-Fast Iterator & Biến `modCount`
* `ArrayList` có một biến đếm cấu trúc: `protected transient int modCount = 0;`.
* Mỗi khi bạn `add()`, `remove()`, `clear()`, biến `modCount++`.
* Khi tạo `Iterator`, nó lưu `expectedModCount = modCount`.
* Trong mỗi vòng lặp `iterator.next()`, nó kiểm tra: nếu `modCount != expectedModCount` $\rightarrow$ Ném ra **`ConcurrentModificationException`** ngay lập tức để ngăn ngừa sai lệch dữ liệu!

---

## 2. `LinkedList` - Cơ Chế Mã Nguồn Bên Trong

```
                     first                                       last
                       │                                           │
                       ▼                                           ▼
null ◄── [prev|item|next] ⇄ [prev|item|next] ⇄ [prev|item|next] ──► null
```

### A. Cấu Trúc Lớp Node Nộ Bộ (Inner Class `Node<E>`)
Mỗi phần tử trong `LinkedList` là một đối tượng `Node` độc lập:
```java
private static class Node<E> {
    E item;         // Dữ liệu thực tế
    Node<E> next;   // Con trỏ trỏ tới Node sau
    Node<E> prev;   // Con trỏ trỏ tới Node trước
}
```
`LinkedList` chỉ giữ 3 thông số: `transient int size`, `transient Node<E> first`, và `transient Node<E> last`.

### B. Tối Ưu Tìm Kiếm Index Trong `LinkedList` (`node(index)`)
Một chi tiết mã nguồn rất hay: Khi bạn gọi `linkedList.get(index)`, Java **KHÔNG duyệt từ đầu đến cuối**!

```java
Node<E> node(int index) {
    // Nếu index nằm ở NỬA ĐẦU danh sách -> Duyệt từ FIRST tiến lên
    if (index < (size >> 1)) {
        Node<E> x = first;
        for (int i = 0; i < index; i++) x = x.next;
        return x;
    } else {
        // Nếu index nằm ở NỬA SAU danh sách -> Duyệt ngược từ LAST lùi lại
        Node<E> x = last;
        for (int i = size - 1; i > index; i--) x = x.prev;
        return x;
    }
}
```
* **Tác dụng:** Giảm một nửa số bước duyệt, nhưng độ phức tạp vẫn là **$O(N)$**.

### C. Gánh Nặng Bộ Nhớ & Áp Lực Lên Garbage Collection (GC)
* Mỗi phần tử trong `ArrayList` chỉ tốn 4 hoặc 8 bytes lưu reference.
* Mỗi phần tử trong `LinkedList` tốn:
  * Overhead của đối tượng `Node` (16 bytes).
  * 3 con trỏ `item`, `next`, `prev` (24 bytes).
  * Tổng cộng tốn **gấp 5-6 lần bộ nhớ RAM** so với `ArrayList` và liên tục tạo rác khiến GC phải hoạt động vất vả.

---

## 3. `Vector` - Lớp Triển Khai Khóa Đồng Bộ Cổ Điển

```java
public synchronized boolean add(E e) {
    modCount++;
    ensureCapacityHelper(elementCount + 1);
    elementData[elementCount++] = e;
    return true;
}
```

* **Bản chất:** Ra đời từ Java 1.0. Bên trong sử dụng mảng tĩnh `Object[] elementData` giống `ArrayList`.
* **Cơ chế Thread-Safety:** **TẤT CẢ** các phương thức (`add`, `get`, `remove`, `size`) đều được đánh dấu từ khóa **`synchronized`**.
* **Thuật toán tăng dung lượng:** Khi đầy mảng, `Vector` tăng gấp **2 lần ($2.0x$)** (khác với `ArrayList` tăng 1.5x).
* **Nhược điểm:** Khóa toàn bộ phương thức gây nghẽn cổ chai nặng nề trong đa luồng. Hiện nay bị thay thế hoàn toàn bởi `ArrayList` hoặc `CopyOnWriteArrayList`.

---

## 4. `Stack` - Lớp Triển Khai LIFO Cổ Điển

```java
public class Stack<E> extends Vector<E> { ... }
```

* **Bản chất:** Kế thừa trực tiếp từ `Vector`, bổ sung 5 hàm LIFO: `push()`, `pop()`, `peek()`, `empty()`, `search()`.
* **Tại sao `Stack` bị coi là thiết kế kém (Deprecated Design)?**
  * Vì kế thừa từ `Vector`, `Stack` vô tình công khai các phương thức của `List` như `add(index, element)` hay `remove(index)`. Điều này cho phép người dùng chèn/xóa phần tử vào giữa Stack, **phá hủy hoàn toàn tính chất LIFO**!
* **Thay thế chuẩn:** Dùng **`ArrayDeque`** (`Deque<E> stack = new ArrayDeque<>()`).

---

## 5. `CopyOnWriteArrayList` - Triển Khai Cho Đa Luồng Hiệu Năng Cao

Nằm trong gói `java.util.concurrent` (từ Java 5).

```java
private transient volatile Object[] array;
final transient ReentrantLock lock = new ReentrantLock();
```

### Mechanisn Copy-On-Write (Sao Chép Khi Ghi):
1. **Thao tác Đọc (`get()`, `iterator()`):** **KHÔNG DÙNG LOCK!** Các luồng đọc trực tiếp trên mảng `volatile Object[] array` hiện tại với tốc độ $O(1)$ tuyệt đối.
2. **Thao tác Ghi (`add()`, `set()`, `remove()`):**
   * Lấy khóa `ReentrantLock`.
   * **Tạo một BẢN SAO HOÀN TOÀN MỚI** của mảng (`Arrays.copyOf`).
   * Thực hiện sửa đổi trên mảng mới.
   * Gán lại tham chiếu `array` sang mảng mới (nhờ từ khóa `volatile`, tất cả luồng đọc thấy ngay mảng mới).
   * Giải phóng khóa.

```java
public boolean add(E e) {
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        Object[] elements = getArray();
        int len = elements.length;
        Object[] newElements = Arrays.copyOf(elements, len + 1); // SAO CHÉP MẢNG MỚI
        newElements[len] = e;
        setArray(newElements); // GÁN LẠI CHỈ SỐ VOLATILE
        return true;
    } finally {
        lock.unlock();
    }
}
```

* **Ứng dụng thực tế:** Dùng cho bài toán **Đọc RẤT NHIỀU - Ghi RẤT ÍT** (ví dụ: Danh sách Observers/Listeners, danh sách Cấu hình hệ thống).
* **Iterator:** Trả về một **Snapshot Iterator** (bức ảnh chụp mảng tại thời điểm tạo iterator), **không bao giờ ném lỗi `ConcurrentModificationException`**.

---

## 📌 Bảng So Sánh Tổng Hợp Tất Cả Triển Khai `List`

| Class | Đa luồng (Thread-Safe)? | Cơ chế bên dưới | Tăng kích thước | Đọc (`get`) | Ghi/Xóa giữa | Thích hợp cho |
| :--- | :---: | :--- | :---: | :---: | :---: | :--- |
| **`ArrayList`** | ❌ Không | Mảng động (`Object[]`) | **1.5x** | **$O(1)$** | $O(N)$ | $95\%$ các bài toán ứng dụng đơn luồng. |
| **`LinkedList`** | ❌ Không | Doubly-Linked List | N/A | $O(N)$ | $O(N)$ (Node access) | Làm Queue / Deque 2 đầu. |
| **`Vector`** |  Có (`synchronized`) | Mảng động (`Object[]`) | **2.0x** | $O(1)$ (locked) | $O(N)$ | Code legacy Java 1.0 (Nên tránh). |
| **`Stack`** |  Có (`synchronized`) | Kế thừa `Vector` | **2.0x** | $O(1)$ | $O(N)$ | LIFO cũ (Nên thay bằng `ArrayDeque`). |
| **`CopyOnWriteArrayList`** |  Có (Copy-on-write) | Mảng `volatile Object[]` | Bản sao mới | **$O(1)$** (Lock-free) | $O(N)$ (Copy array) | Ứng dụng đa luồng: Read nhiều - Write ít. |

---
*Tài liệu này hoàn chỉnh sâu sắc mã nguồn vận hành của toàn bộ các lớp List!*
