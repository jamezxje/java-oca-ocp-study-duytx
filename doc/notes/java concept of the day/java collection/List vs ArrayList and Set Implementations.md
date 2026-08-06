# Chuyên Sâu Interface List vs ArrayList và Họ Nhà Set Internals

Tài liệu này hệ thống hóa toàn bộ kiến thức nâng cao về **Interface `List` vs `ArrayList`, Cơ chế vận hành bên trong (Under the hood) của các triển khai `Set` (`HashSet`, `LinkedHashSet`, `TreeSet`) và Các cạm bẫy thực tế**.

---

## Phần 1: Interface `List` vs Lớp Triển Khai `ArrayList`

### 1. `List` Interface là gì?
* **`List<E>`** là một Interface trong gói `java.util`, mở rộng từ `Collection<E>`.
* Đại diện cho một **danh sách có thứ tự (Ordered Sequence)**:
  * Cho phép lưu trữ các phần tử **trùng lặp (Duplicates)**.
  * Cho phép truy cập phần tử dựa trên **Chỉ số Vị trí (Index-based access)**: `get(index)`, `set(index, element)`.
  * Cho phép chứa nhiều phần tử `null`.

---

### 2. Tư Duy Thiết Kế: `List<String> list = new ArrayList<>()` vs `ArrayList<String> list = new ArrayList<>()`

Một câu hỏi phỏng vấn cơ bản nhưng đo tư duy lập trình: **"Tại sao lại khai báo kiểu biến là Interface `List` thay vì kiểu cụ thể `ArrayList`?"**

```java
// ✅ NÊN DÙNG: Lập trình theo Interface (Programming to Interface)
List<String> list1 = new ArrayList<>();

// ❌ KHÔNG NÊN DÙNG: Ràng buộc chặt chẽ vào lớp cụ thể (Tight Coupling)
ArrayList<String> list2 = new ArrayList<>();
```

#### Lợi ích của việc dùng Interface `List`:
1. **Tính Linh Hoạt & Đổi Lớp Dễ Dàng (Loose Coupling):**
   Nếu sau này bài toán thay đổi cần dùng `LinkedList`, `CopyOnWriteArrayList` (cho đa luồng) hay `Vector`, bạn chỉ cần đổi duy nhất 1 chỗ khởi tạo `new LinkedList<>()`. Toàn bộ mã nguồn phía sau gọi các hàm của `List` giữ nguyên không phải sửa một dòng nào!
2. **Thiết Kế API Chuẩn:**
   Các phương thức (Methods) nên nhận tham số và trả về kiểu `List<T>` để phía Caller có thể truyền vào bất kỳ loại List nào họ muốn.

---

### 3. Cạm Bẫy Lắt Léo Với Các Hàm Tạo `List`

#### ⚠️ Bẫy 1: `Arrays.asList(array)`
* `Arrays.asList()` trả về một `List` có kích thước **CỐ ĐỊNH (Fixed-size List)** được bọc trực tiếp từ mảng gốc.
* **Hậu quả:** 
  * Nếu gọi `list.add("X")` hoặc `list.remove(0)` $\rightarrow$ Thăng hoa ném ra **`UnsupportedOperationException`**!
  * Thay đổi phần tử trong `list` sẽ làm **thay đổi luôn phần tử trong mảng gốc**.
* *Cách sửa để tạo List động:* `List<String> list = new ArrayList<>(Arrays.asList(array));`

#### ⚠️ Bẫy 2: `List.of(...)` (Java 9+)
* `List.of()` trả về một **Unmodifiable List (List Bất Biến hoàn toàn)**.
* Không thể `add()`, `remove()`, `set()`.
* **Cấm tuyệt đối `null`:** Nếu truyền `null` vào `List.of("A", null)` $\rightarrow$ Ném ra `NullPointerException` ngay lập tức!

#### ⚠️ Bẫy 3: Phương thức `subList(fromIndex, toIndex)`
* `subList()` **KHÔNG tạo ra một List mới**, nó chỉ trả về một **Cửa sổ hiển thị (VIEW)** nhìn vào đoạn dữ liệu của List gốc.
* Nếu bạn sửa phần tử trong `subList`, List gốc cũng bị sửa theo.
* Nếu sau khi tạo `subList`, bạn lại thêm/xóa phần tử ở List gốc $\rightarrow$ Mọi thao tác trên `subList` sau đó sẽ ném ra **`ConcurrentModificationException`**!

---

## Phần 2: Họ Nhà `Set` - Cơ Chế Vận Hành Bên Trong (Under The Hood)

Interface **`Set<E>`** đại diện cho một tập hợp **KHÔNG chứa phần tử trùng lặp** (`e1.equals(e2) == false`).

---

### 1. `HashSet` - Vận Hành Bên Trong Ra Sao?

```
HashSet<E> ──(Under the hood)──> Bọc một HashMap<E, Object>
```

* **Bản chất:** `HashSet` **KHÔNG hề có thuật toán băm riêng**. Khi bạn khởi tạo `new HashSet<>()`, bên trong nó thực chất khởi tạo một `new HashMap<>()`.
* **Cơ chế lưu trữ:**
  * Mỗi phần tử `element` thêm vào `HashSet` sẽ được đặt làm **KEY trong `HashMap`**.
  * **VALUE trong `HashMap`** là một hằng số giả định `PRESENT = new Object()`.
* **Cách chống trùng lặp:**
  * Khi gọi `set.add(e)`, bên dưới gọi `map.put(e, PRESENT)`.
  * `HashMap` sẽ dùng `e.hashCode()` để tìm Bucket và dùng `e.equals()` để kiểm tra xem Key `e` đã tồn tại chưa.
  * Nếu Key đã tồn tại, `map.put()` trả về `PRESENT` $\rightarrow$ `set.add()` nhận diện là bị trùng và trả về `false`.
* **Đặc điểm:** Độ phức tạp **$O(1)$**, **không giữ thứ tự phần tử**, cho phép tối đa 1 phần tử `null`.

---

### 2. `LinkedHashSet` - Giữ Thứ Tự Chèn Vào Như Thế Nào?

```
LinkedHashSet<E> ──(Under the hood)──> Kế thừa HashSet, bọc LinkedHashMap<E, Object>
```

* **Bản chất:** Kế thừa từ `HashSet` nhưng bên dưới được hỗ trợ bởi **`LinkedHashMap`**.
* **Cơ chế giữ thứ tự:**
  * Ngoài mảng Buckets của `HashMap`, mỗi Entry trong `LinkedHashSet` có thêm 2 con trỏ `before` và `after` tạo thành một **Danh sách liên kết đôi (Doubly-Linked List)** chạy qua tất cả các phần tử.
  * Khi bạn duyệt `LinkedHashSet` (qua `for-each` hay `Iterator`), nó duyệt theo con trỏ `before`/`after` này $\rightarrow$ Đảm bảo dữ liệu lấy ra **ĐÚNG THEO THỨ TỰ ĐÃ CHÈN VÀO (Insertion Order)**.
* **Đặc điểm:** Độ phức tạp **$O(1)$**, giữ thứ tự chèn, tốn bộ nhớ hơn `HashSet` một chút do lưu con trỏ liên kết.

---

### 3. `TreeSet` - Cây Đỏ-Đen & Cạm Bẫy So Sánh

```
TreeSet<E> ──(Under the hood)──> Implement NavigableSet, bọc TreeMap<E, Object> (Red-Black Tree)
```

* **Bản chất:** Được hỗ trợ bên dưới bởi **`TreeMap`**, một cấu trúc dữ liệu **Red-Black Tree (Cây tự cân bằng)**.
* **Cơ chế tự sắp xếp:**
  * Tất cả các phần tử được tự động sắp xếp theo **Thứ tự tự nhiên (Natural Order)** thông qua `Comparable` (hàm `compareTo()`) hoặc qua `Comparator` truyền vào khi tạo `TreeSet`.
* ** CẠM BẪY CHẾT NGƯỜI: `TreeSet` KHÔNG DÙNG `equals()` hay `hashCode()`!**
  * Đây là điểm bẫy phỏng vấn cực mạnh! `HashSet` chống trùng bằng `equals()`, nhưng `TreeSet` chống trùng hoàn toàn dựa vào kết quả của **`compareTo() == 0`** (hoặc `compare() == 0`).
  * Nếu 2 đối tượng có `a.equals(b) == false` nhưng hàm `compareTo()` của chúng trả về `0`, `TreeSet` sẽ coi 2 đối tượng đó là trùng nhau và **KHÔNG CHO CHÈN** phần tử thứ 2 vào!

#### Ví dụ chứng minh bẫy `TreeSet`:
```java
class Student implements Comparable<Student> {
    int id;
    String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // compareTo CHỈ SO SÁNH 'id'
    @Override
    public int compareTo(Student o) {
        return Integer.compare(this.id, o.id);
    }
}

public class Main {
    public static void main(String[] args) {
        Set<Student> treeSet = new TreeSet<>();
        Student s1 = new Student(1, "Alice");
        Student s2 = new Student(1, "Bob"); // id giống nhau, name khác nhau

        treeSet.add(s1);
        treeSet.add(s2);

        System.out.println(treeSet.size()); // ❌ KẾT QUẢ: 1 !!!
        // s2 không được chèn vào vì compareTo() trả về 0!
    }
}
```

* **Đặc điểm:** Độ phức tạp **$O(\log N)$** cho `add`, `remove`, `contains`. **Cấm tuyệt đối `null`** (vì ném `NullPointerException` khi gọi `null.compareTo()`).
* **Các hàm định vị cao cấp (Navigable API):** `first()`, `last()`, `higher(e)`, `lower(e)`, `ceiling(e)`, `floor()`.

---

## 📌 Bảng So Sánh Tổng Hợp 3 Loại `Set`

| Tiêu chí | `HashSet` | `LinkedHashSet` | `TreeSet` |
| :--- | :--- | :--- | :--- |
| **Cấu trúc dữ liệu bên dưới** | `HashMap` (Mảng Buckets) | `LinkedHashMap` (HashMap + Doubly-linked list) | `TreeMap` (Cây Đỏ-Đen / Red-Black Tree) |
| **Thứ tự phần tử** | Không cố định | Đúng **thứ tự chèn vào** (Insertion Order) | Đúng **thứ tự sắp xếp** (Sorted Order) |
| **Độ phức tạp** | **$O(1)$** | **$O(1)$** | **$O(\log N)$** |
| **Cơ chế chống trùng** | Dùng `hashCode()` + `equals()` | Dùng `hashCode()` + `equals()` | Dùng **`compareTo() == 0`** hoặc `compare() == 0` |
| **Chứa `null`?** | Cho phép 1 `null` | Cho phép 1 `null` | ❌ **Cấm `null`** |

---
*Tài liệu này hoàn chỉnh bức tranh chuyên sâu về List & các tập hợp Set!*
