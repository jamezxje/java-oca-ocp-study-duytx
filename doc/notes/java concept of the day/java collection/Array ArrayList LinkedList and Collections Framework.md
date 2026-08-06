# Array, ArrayList, LinkedList & Java Collections Framework

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Cấu trúc dữ liệu mảng, List, Queue, Set và Toàn bộ Java Collections Framework** dành cho lập trình viên Java chuẩn bị phỏng vấn và thi chứng chỉ OCA/OCP.

---

## Phần 1: Tổng Quan Cây Kế Thừa Java Collections Framework

```
                          java.util.Collection
                                    │
         ┌──────────────────────────┼──────────────────────────┐
         ▼                          ▼                          ▼
  java.util.List             java.util.Set              java.util.Queue
  - ArrayList                - HashSet                  - ArrayDeque
  - LinkedList               - LinkedHashSet            - PriorityQueue
  - Vector / Stack           - TreeSet (NavigableSet)   - LinkedList
                                                               │
                                                               ▼
                                                        java.util.Deque
                                                        - ArrayDeque
                                                        - LinkedList

* Lưu ý: java.util.Map KHÔNG kế thừa từ java.util.Collection! Map là một cấu trúc dữ liệu cặp Key-Value độc lập.
```

---

## Phần 2: So Sánh Chi Tiết: Array vs ArrayList vs LinkedList

### 1. Array vs `ArrayList`

| Tiêu chí | Array (Mảng tĩnh `T[]`) | `ArrayList` (Mảng động) |
| :--- | :--- | :--- |
| **Kích thước** | **Cố định** ngay khi khởi tạo (không thể tăng/giảm). | **Động** (Tự động mở rộng kích thước khi đầy). |
| **Kiểu dữ liệu** | Hỗ trợ cả **Primitive** (`int[]`) và Object (`String[]`). | **Chỉ hỗ trợ Object** (`ArrayList<Integer>`). |
| **Hiệu năng** | Cực kỳ nhanh, tốn ít bộ nhớ nhất. | Chậm hơn một chút do chi phí Autoboxing & Resizing. |
| **Tính năng** | Dùng toán tử `[]` và thuộc tính `.length`. | Cung cấp các hàm phong phú `.add()`, `.remove()`, `.size()`. |

#### Cơ chế tự mở rộng kích thước của `ArrayList`:
* `ArrayList` bên trong sử dụng mảng tĩnh: `transient Object[] elementData`.
* Dung lượng mặc định ban đầu (**Initial Capacity**): `10`.
* Khi số lượng phần tử vượt quá dung lượng hiện tại, `ArrayList` sẽ tạo mảng mới có dung lượng tăng **$1.5$ lần**:
  $$\text{New Capacity} = \text{Old Capacity} + (\text{Old Capacity} \gg 1)$$
* Sao chép toàn bộ phần tử cũ sang mảng mới bằng `Arrays.copyOf()`.

---

### 2. `ArrayList` vs `LinkedList` (CÂU HỎI PHỎNG VẤN HÀNG ĐẦU)

| Tiêu chí | `ArrayList` | `LinkedList` |
| :--- | :--- | :--- |
| **Cấu trúc bên dưới** | Mảng động (`Object[]`). | Danh sách liên kết đôi (Doubly-Linked List `Node<E>`). |
| **Truy cập ngẫu nhiên (`get(index)`)** | **$O(1)$** (Nhảy thẳng đến ô nhớ index). | **$O(N)$** (Phải duyệt từ đầu hoặc cuối đến index). |
| **Chèn / Xóa ở ĐẦU danh sách** | **$O(N)$** (Phải dời toàn bộ phần tử sang phải). | **$O(1)$** (Chỉ cần đổi con trỏ `head`). |
| **Chèn / Xóa ở CUỐI danh sách** | **$O(1)$** (amortized). | **$O(1)$** (Đổi con trỏ `tail`). |
| **Chèn / Xóa ở GIỮA danh sách** | **$O(N)$** (Shift phần tử). | **$O(N)$** (Thời gian tìm node $O(N)$, nối con trỏ $O(1)$). |
| **Dung lượng bộ nhớ** | Tiết kiệm hơn (Chỉ lưu phần tử). | Tốn bộ nhớ hơn (Mỗi Node tốn thêm 2 con trỏ `prev` và `next`). |
| **CPU Cache Locality** | **Cực tốt (Spatial Locality)** do mảng liên tục. | **Tệ (Cache Miss)** do Node nằm rải rác trên Heap. |

> **Lời khuyên thực tế từ Senior:** Trong $95\%$ các bài toán dự án thực tế, **luôn ưu tiên dùng `ArrayList`**. Kể cả khi chèn/xóa nhiều, chi phí dời mảng trên RAM của `ArrayList` nhờ CPU Cache vẫn nhanh hơn chi phí cấp phát bộ nhớ và tìm kiếm Node rải rác của `LinkedList`!

---

## Phần 3: Legacy Collections: `Vector` và `Stack`

* **`Vector`**: Phiên bản đồng bộ (`synchronized`) cũ của `ArrayList` từ Java 1.0. Tăng dung lượng gấp **2 lần** khi đầy. Hiện nay không nên dùng vì nghẽn hiệu năng đa luồng.
* **`Stack`**: Kế thừa từ `Vector` để làm cấu trúc LIFO (Last In First Out).
  * **Tại sao không nên dùng `Stack` nữa?** `Stack` kế thừa từ `Vector` nên bị kế thừa luôn các hàm của `List` như `add(index, element)`, làm vi phạm nguyên tắc LIFO của Stack!
  * **Thay thế chuẩn:** Dùng **`ArrayDeque`** cho cấu trúc Stack/Queue (`Deque<String> stack = new ArrayDeque<>()`).

---

## Phần 4: Hàng Đợi Queue, Deque và PriorityQueue

### 1. `Queue` (FIFO - First In First Out)
* Các phương thức chính: `offer()` (thêm), `poll()` (lấy và xóa), `peek()` (xem đầu hàng).

### 2. `Deque` (Double Ended Queue - Hàng đợi 2 đầu)
* Cho phép thêm/xóa/xem ở **cả 2 đầu** (Đầu Head và Đuôi Tail).
* **`ArrayDeque`**: Cấu trúc hàng đợi 2 đầu dạng mảng vòng (Resizable Array). **Nhanh hơn `LinkedList`** khi làm Queue/Stack vì không bị tốn chi phí rác GC cho các Node.

### 3. `PriorityQueue` (Hàng đợi ưu tiên)
* Cấu trúc dữ liệu bên dưới là **Min-Heap (Cây vun đống)**.
* Phần tử lấy ra đầu tiên luôn là phần tử nhỏ nhất (hoặc theo quy tắc của `Comparator` truyền vào).
* Không cho phép chứa phần tử `null`.

---

## Phần 5: Họ Nhà `Set` (Không Chứa Phần Tử Trùng Lặp)

| Lớp triển khai | Cấu trúc dữ liệu | Thứ tự phần tử | Độ phức tạp | Chứa `null`? |
| :--- | :--- | :--- | :--- | :--- |
| **`HashSet`** | Bọc một `HashMap`. | **Không cố định** (phụ thuộc hash code). | **$O(1)$** | Cho phép 1 phần tử `null`. |
| **`LinkedHashSet`** | Bọc `LinkedHashMap` (Mảng băm + Doubly-linked list). | Giữ đúng **thứ tự chèn vào (Insertion Order)**. | **$O(1)$** | Cho phép 1 phần tử `null`. |
| **`TreeSet`** | Bọc `TreeMap` (Red-Black Tree). | Sắp xếp theo **thứ tự (Sorted Order)** tự nhiên hoặc qua `Comparator`. | **$O(\log N)$** | ❌ **Cấm `null`** (vì không thể `compareTo` với null). |

---

## 📌 Bảng Tóm Tắt Độ Phức Tạp (Cheatsheet Cho Phỏng Vấn)

| Collection | Access (`get`) | Search | Insert | Delete | Dữ liệu trùng? | Thứ tự giữ? |
| :--- | :--- | :--- | :--- | :--- | :---: | :---: |
| **`ArrayList`** | $O(1)$ | $O(N)$ | $O(1)$ tail / $O(N)$ middle | $O(N)$ |  Có |  Thứ tự chèn |
| **`LinkedList`** | $O(N)$ | $O(N)$ | $O(1)$ head/tail | $O(1)$ head/tail |  Có |  Thứ tự chèn |
| **`HashSet`** | N/A | $O(1)$ | $O(1)$ | $O(1)$ | ❌ Không | ❌ Không |
| **`TreeSet`** | N/A | $O(\log N)$ | $O(\log N)$ | $O(\log N)$ | ❌ Không |  Thứ tự sắp xếp |
| **`ArrayDeque`** | N/A | $O(N)$ | $O(1)$ head/tail | $O(1)$ head/tail |  Có |  Thứ tự chèn |

---
*Tài liệu này tổng hợp toàn bộ Java Collections Framework & Cấu trúc dữ liệu!*
