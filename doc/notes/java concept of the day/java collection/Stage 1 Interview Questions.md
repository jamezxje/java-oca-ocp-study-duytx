# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 1: Java Collections & Hash Mechanism

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên).

---

## Góc 1: Hợp Đồng `equals()` và `hashCode()`

### ❓ Câu 1: Tại sao khi override `equals()` BẮT BUỘC phải override `hashCode()`?
* **Mục đích hỏi:** Kiểm tra xem ứng viên nắm bản chất kiểm tra 2 bước của Hash-based Collections hay chỉ viết code theo thói quen.
* **Gợi ý trả lời chuẩn:**
  * Các cấu trúc băm (`HashMap`, `HashSet`) tìm kiếm phần tử qua **2 bước**:
    1. **Bước 1:** Gọi `key.hashCode()` để tính Index và tìm đến đúng **Bucket (thùng)** chứa dữ liệu ($O(1)$).
    2. **Bước 2:** Khi đã vào đúng Bucket, mới gọi `equals()` để so sánh chính xác từng phần tử trong Bucket đó.
  * Nếu chỉ override `equals()` mà quên `hashCode()`, hai đối tượng giống hệt nhau về thuộc tính sẽ sinh ra 2 `hashCode()` khác nhau (dùng địa chỉ bộ nhớ mặc định của `Object`).
  * Kết quả: `HashMap` tìm kiếm ở 2 Bucket khác nhau $\rightarrow$ `map.get(key)` trả về `null` $\rightarrow$ **Thất lạc dữ liệu trong Map**.

### ❓ Câu 2: Chuyện gì xảy ra nếu tôi dùng một Mutable Object (đối tượng có thể sửa đổi) làm Key trong `HashMap`?
* **Mục đích hỏi:** Đánh giá tư duy về Immutability và Side Effects.
* **Gợi ý trả lời chuẩn:**
  * Khi chèn đối tượng vào `HashMap`, nó nằm ở Bucket dựa trên `hashCode()` ban đầu.
  * Nếu sau đó thuộc tính của Key bị thay đổi (qua setter), giá trị `hashCode()` của Key sẽ bị tính toán ra một con số khác.
  * Khi gọi `map.get(key)`, `HashMap` sẽ tìm ở Bucket mới tương ứng với hash code mới $\rightarrow$ Trả về `null`.
  * **Hậu quả:** Dữ liệu cũ bị kẹt vĩnh viễn ở Bucket cũ mà không thể `get()` ra hay `remove()` đi được $\rightarrow$ Trực tiếp gây ra **Memory Leak**.
  * **Giải pháp:** Key nên là **Immutable Class** (`String`, `Integer`, `Long`, `UUID`...).

---

## Góc 2: Kiến Trúc Bên Trong của `HashMap`

### ❓ Câu 3: Hãy trình bày luồng xử lý của `map.put(key, value)` từ Java 8 trở đi?
* **Mục đích hỏi:** Kiểm tra kiến thức về Cấu trúc dữ liệu & Thuật toán bên trong JVM.
* **Gợi ý trả lời chuẩn:**
  1. **Tính Hash Code:** Tính `hash(key) = (h = key.hashCode()) ^ (h >>> 16)` để làm đều các bít.
  2. **Tính Index:** Dùng công thức bít `index = (n - 1) & hash`.
  3. **Chèn dữ liệu:**
     * Nếu Bucket rỗng: Tạo `Node` mới chèn vào.
     * Nếu đụng độ Hash (Collision): Duyệt qua danh sách. Nếu trùng Key thì overwrite Value. Nếu không trùng thì chèn vào **cuối danh sách (Tail Insertion)**.
  4. **Cây hóa (Treeification):** Nếu 1 Bucket chứa $\ge 8$ phần tử và tổng capacity $\ge 64$, chuyển LinkedList thành **Red-Black Tree** ($O(\log N)$).
  5. **Resize:** Nếu `size > capacity * loadFactor` ($16 \times 0.75 = 12$), tăng gấp đôi dung lượng mảng ($16 \rightarrow 32$) và Rehash.

### ❓ Câu 4: Tại sao dung lượng của `HashMap` luôn luôn là lũy thừa của 2 ($16, 32, 64\dots$)?
* **Mục đích hỏi:** Đánh giá sự hiểu biết về tối ưu hóa cấp độ CPU & Toán tử bitwise.
* **Gợi ý trả lời chuẩn:**
  * Khi dung lượng mảng $n$ là lũy thừa của 2 ($n = 2^k$), toán tử bít `(n - 1) & hash` cho kết quả **chính xác bằng phép chia lấy dư `hash % n`**.
  * Toán tử bít `&` chạy trên CPU nhanh hơn phép chia `%` gấp nhiều lần.
  * Giúp các bít của hash code phân bổ cực kỳ đồng đều vào tất cả các index trong mảng.

### ❓ Câu 5: Sự khác biệt về hiệu năng Worst-Case của `HashMap` giữa Java 7 và Java 8?
* **Mục đích hỏi:** Kiểm tra việc cập nhật kiến thức Java 8.
* **Gợi ý trả lời chuẩn:**
  * **Java 7:** Trường hợp xấu nhất (tất cả Key rơi vào 1 Bucket) có độ phức tạp là **$O(N)$** vì dùng LinkedList. Dễ bị tấn công Hash Collision DoS.
  * **Java 8:** Trường hợp xấu nhất giảm xuống **$O(\log N)$** nhờ cơ chế tự động chuyển LinkedList sang **Red-Black Tree** khi Bucket $\ge 8$ phần tử.

---

## Góc 3: Đa Luồng & Concurrent Collections

### ❓ Câu 6: So sánh `HashMap`, `Hashtable`, `Collections.synchronizedMap()` và `ConcurrentHashMap`?
* **Mục đích hỏi:** Đo độ hiểu biết về Thread-safety & Locking strategy.
* **Gợi ý trả lời chuẩn:**
  * `HashMap`: Không Thread-safe, nhanh nhất khi chạy đơn luồng.
  * `Hashtable` & `synchronizedMap`: Thread-safe nhưng dùng **Global Lock** (khóa toàn bộ Map khi đọc/ghi) $\rightarrow$ Nghẽn cổ chai nặng nề.
  * `ConcurrentHashMap` (Java 8+): Thread-safe với **Lock Striping**. Sử dụng **CAS** cho bucket rỗng + **`synchronized` trên Bucket Head Node** khi chèn. Đọc (`get`) không tốn lock nhờ `volatile`.

### ❓ Câu 7: Tại sao `ConcurrentHashMap` lại cấm `null` Key và `null` Value?
* **Mục đích hỏi:** Kiểm tra kiến thức về Race Condition trong môi trường đa luồng.
* **Gợi ý trả lời chuẩn:**
  * Trong môi trường đơn luồng (`HashMap`), khi `map.get("key")` ra `null`, ta có thể gọi `map.containsKey("key")` để phân biệt *"Key không tồn tại"* hay *"Key tồn tại nhưng value = null"*.
  * Trong môi trường đa luồng (`ConcurrentHashMap`), giữa 2 lời gọi `get()` và `containsKey()`, một Thread khác có thể đã sửa hoặc xóa Key đó $\rightarrow$ Gây ra sự mập mờ dữ liệu (Ambiguity / Race Condition).
  * Do đó, tác giả Doug Lea đã cấm `null` để đảm bảo tính nhất quán tuyệt đối.

---

## Góc 4: So Sánh Collections Khác

### ❓ Câu 8: So sánh `ArrayList` và `LinkedList`? Tại sao thực tế nên ưu tiên `ArrayList`?
* **Mục đích hỏi:** Kiểm tra kiến thức về CPU Cache Locality (Thực tế phần cứng).
* **Gợi ý trả lời chuẩn:**
  * `ArrayList`: Dựa trên mảng động. Truy cập ngẫu nhiên $O(1)$, chèn/xóa ở giữa $O(N)$.
  * `LinkedList`: Dựa trên danh sách liên kết đôi. Chèn/xóa ở đầu/cuối $O(1)$, truy cập $O(N)$.
  * **Lý do ưu tiên `ArrayList` trong thực tế:**
    1. **CPU Cache Line (Spatial Locality):** Mảng lưu các phần tử liên tục trong bộ nhớ, CPU đọc một block vào Cache giúp duyệt `ArrayList` nhanh hơn `LinkedList` rất nhiều. Các Node của `LinkedList` nằm phân tán rải rác trên Heap $\rightarrow$ Gây Cache Miss liên tục.
    2. `LinkedList` tốn bộ nhớ hơn vì mỗi Node phải lưu thêm 2 con trỏ `prev` và `next`.

### ❓ Câu 9: Phân biệt `Comparable` và `Comparator`?
* **Mục đích hỏi:** Sử dụng trong `TreeMap`, `TreeSet` hoặc `Collections.sort()`.
* **Gợi ý trả lời chuẩn:**
  * `Comparable`: Định nghĩa **thứ tự sắp xếp tự nhiên (Natural Ordering)** của class. Khai báo phương thức `compareTo(T o)` ngay bên trong class đó.
  * `Comparator`: Định nghĩa **thứ tự sắp xếp tùy chỉnh (Custom Ordering)**. Là một `Functional Interface` độc lập bên ngoài, có phương thức `compare(T o1, T o2)`. Rất linh hoạt khi muốn sắp xếp 1 đối tượng theo nhiều tiêu chí khác nhau (theo tên, theo tuổi, theo lương) hoặc dùng với Lambda Expression.

---
*Tài liệu này giúp bạn tự tin trả lời bất kỳ câu hỏi nào về Java Collections khi đi phỏng vấn!*
