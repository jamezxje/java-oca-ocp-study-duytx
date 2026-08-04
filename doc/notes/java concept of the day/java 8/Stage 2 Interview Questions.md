# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 2: Java 8+ Stream API & Functional Programming

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên) về Java 8+.

---

## ❓ Câu 1: Phân biệt sự khác nhau giữa `map()` và `flatMap()`? Khi nào nên dùng loại nào?
* **Mục đích hỏi:** Kiểm tra kiến thức biến đổi luồng dữ liệu trong Stream API.
* **Gợi ý trả lời chuẩn:**
  * `map()` biến đổi các phần tử theo tỷ lệ $1 \rightarrow 1$. Hàm truyền vào là `Function<T, R>`, kết quả trả về là `Stream<R>`. Thường dùng khi chuyển đổi thuộc tính đối tượng (ví dụ: `Employee` $\rightarrow$ `String name`).
  * `flatMap()` dùng để **phẳng hóa (flatten)** cấu trúc dữ liệu lồng nhau theo tỷ lệ $1 \rightarrow N$. Hàm truyền vào là `Function<T, Stream<R>>`, kết quả gộp nhiều Stream nhỏ thành 1 `Stream<R>` duy nhất.
  * Ví dụ: Khi có `List<Order>`, mỗi `Order` chứa `List<Item>`, muốn lấy tất cả các `Item` không trùng lặp thì phải dùng `flatMap(order -> order.getItems().stream())`.

---

## ❓ Câu 2: Tại sao biến cục bộ (Local Variable) khi dùng trong Lambda Expression bắt buộc phải là `final` hoặc `effectively final`?
* **Mục đích hỏi:** Đo độ hiểu biết về mô hình bộ nhớ JVM (Stack vs Heap) và cơ chế *Variable Capture*.
* **Gợi ý trả lời chuẩn:**
  * Biến cục bộ sống trên **Stack Frame** của phương thức và sẽ bị xóa khi phương thức kết thúc. Trong khi đó, Lambda Expression có thể được thực thi异步 trên một Thread khác có vòng đời dài hơn.
  * Để giải quyết điều này, Java thực hiện **Variable Capture** - tạo một **bản sao (copy)** của biến đó vào trong Lambda.
  * Để tránh xung đột và mất đồng bộ dữ liệu giữa bản gốc trên Stack và bản sao trong Lambda, Java quy định biến đó không được phép thay đổi giá trị (`effectively final`).

---

## ❓ Câu 3: Khái niệm "Lazy Evaluation" (Đánh giá lười biếng) trong Stream API là gì và nó giúp tối ưu hiệu năng ra sao?
* **Mục đích hỏi:** Đánh giá sự hiểu biết về cơ chế thực thi bên dưới của Stream.
* **Gợi ý trả lời chuẩn:**
  * Các thao tác trung gian như `filter()`, `map()`, `sorted()` không hề xử lý dữ liệu ngay khi gọi, mà chỉ xây dựng một pipeline kế hoạch thực thi.
  * Toàn bộ pipeline chỉ thực sự chạy khi một **Terminal Operation** (`collect()`, `findFirst()`, `count()`) được gọi.
  * **Tối ưu hiệu năng:** Giúp Stream áp dụng cơ chế **Loop Fusion** (gộp nhiều filter/map vào một vòng lặp duy nhất) và **Short-circuiting** (dừng ngay khi tìm thấy kết quả thỏa mãn, ví dụ `findFirst()`), tránh việc duyệt qua toàn bộ danh sách nhiều lần.

---

## ❓ Câu 4: Phân biệt `Optional.orElse()` và `Optional.orElseGet()`? Tại sao viết `optional.orElse(new ExpensiveObject())` lại là một cạm bẫy hiệu năng?
* **Mục đích hỏi:** Kiểm tra kinh nghiệm viết code sạch & tối ưu hiệu năng thực tế.
* **Gợi ý trả lời chuẩn:**
  * `orElse(T other)`: Luôn luôn khởi tạo/thực thi tham số `other` **ngay tại thời điểm gọi**, bất kể `Optional` có chứa dữ liệu hay rỗng.
  * `orElseGet(Supplier<T> supplier)`: Chỉ khởi tạo/thực thi hàm `supplier` **khi và chỉ khi `Optional` bị rỗng** (Lazy Evaluation).
  * Nếu dùng `orElse(new ExpensiveObject())`, đối tượng `ExpensiveObject` sẽ luôn được khởi tạo gây lãng phí bộ nhớ và CPU ngay cả khi `Optional` đã có dữ liệu.

---

## ❓ Câu 5: Khi nào KHÔNG NÊN sử dụng `Parallel Stream`?
* **Mục đích hỏi:** Đánh giá xem ứng viên có hiểu về Concurrency và rủi ro phần cứng thực tế hay không.
* **Gợi ý trả lời chuẩn:**
  * **Không dùng khi có I/O Blocking:** Parallel Stream sử dụng chung pool luồng **`ForkJoinPool.commonPool()`** của toàn bộ JVM. Nếu gọi API ngoài hoặc truy vấn DB trong Parallel Stream, các luồng bị block sẽ làm nghẽn toàn bộ ứng dụng.
  * **Không dùng với tập dữ liệu nhỏ:** Chi phí phân tách dữ liệu (Spliterator) và gộp luồng lớn hơn chi phí chạy tuần tự.
  * **Không dùng với cấu trúc khó phân tách:** Ví dụ `LinkedList` (không thể chia đôi hiệu quả như `ArrayList`).
  * **Không dùng khi thao tác trên State có thể thay đổi (Shared Mutable State):** Dễ gây bẫy Race Condition.

---

## ❓ Câu 6: Phân biệt `Supplier`, `Consumer`, `Predicate`, `Function`?
* **Mục đích hỏi:** Kiểm tra kiến thức nền tảng về Functional Interfaces.
* **Gợi ý trả lời chuẩn:**
  * `Supplier<T>`: Không nhận tham số, trả về `T` (`T get()`). Dùng để tạo/cung cấp dữ liệu.
  * `Consumer<T>`: Nhận `T`, không trả về giá trị (`void accept(T t)`). Dùng để tiêu thụ/ghi log/in ấn.
  * `Predicate<T>`: Nhận `T`, trả về `boolean` (`boolean test(T t)`). Dùng để lọc điều kiện.
  * `Function<T, R>`: Nhận `T`, trả về `R` (`R apply(T t)`). Dùng để chuyển đổi dữ liệu.

---
*Tài liệu này giúp bạn tự tin làm chủ toàn bộ câu hỏi phỏng vấn Java 8+!*
