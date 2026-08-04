# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 4: Generics & Wildcards

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên) về Generics.

---

## ❓ Câu 1: Cơ chế Type Erasure (Xóa bỏ kiểu) trong Java là gì? Tại sao mã nguồn `if (list instanceof List<String>)` lại bị LỖI BIÊN DỊCH?
* **Mục đích hỏi:** Kiểm tra sự hiểu biết về cách JVM thực thi Generics tại Runtime.
* **Gợi ý trả lời chuẩn:**
  * Type Erasure là cơ chế mà Trình biên dịch Java xóa toàn bộ thông tin kiểu Generics sau khi đã kiểm tra an toàn kiểu ở bước Compile-time, chuyển tất cả về `Object` (hoặc Bounded type).
  * Do đó, tại thời điểm Runtime, JVM **không hề biết** danh sách đó được khai báo là `List<String>` hay `List<Integer>`, tất cả chỉ là raw type `List`.
  * Vì thông tin kiểu `<String>` không còn tồn tại lúc Runtime, câu lệnh `instanceof List<String>` bị **Lỗi biên dịch**. Ta chỉ có thể kiểm tra `instanceof List<?>`.

---

## ❓ Câu 2: Giải thích nguyên lý PECS (Producer Extends, Consumer Super)? Cho ví dụ thực tế?
* **Mục đích hỏi:** Đây là câu hỏi kinh điển đánh giá kỹ năng thiết kế Generic Library / API.
* **Gợi ý trả lời chuẩn:**
  * **PECS** viết tắt của **P**roducer **E**xtends, **C**onsumer **S**uper.
  * **`? extends T` (Producer):** Dùng khi ta chỉ muốn **ĐỌC dữ liệu ra** từ collection. Ta không thể gọi `add()` thêm phần tử mới vì Compiler không đảm bảo được kiểu dữ liệu chính xác của mảng.
  * **`? super T` (Consumer):** Dùng khi ta chỉ muốn **GHI (thêm) dữ liệu vào** collection. Ta có thể an toàn gọi `add(T)`.
  * Ví dụ chuẩn trong Java SDK: Phương thức `Collections.copy(List<? super T> dest, List<? extends T> src)`. Trong đó `src` là Producer (chỉ đọc) còn `dest` là Consumer (chỉ ghi).

---

## ❓ Câu 3: Tại sao chúng ta không thể khởi tạo một mảng Generics `new T[10]` hoặc `new List<String>[10]`?
* **Mục đích hỏi:** Đánh giá sự xung đột giữa cơ chế Mảng (Arrays) và Generics.
* **Gợi ý trả lời chuẩn:**
  * Mảng trong Java có tính **Reifiable** (giữ nguyên thông tin kiểu tại Runtime) và **Covariant** (`String[]` là con của `Object[]`).
  * Generics trong Java lại bị **Type Erasure** (xóa kiểu tại Runtime) và **Invariant** (`List<String>` KHÔNG phải là con của `List<Object>`).
  * Nếu Java cho phép tạo mảng Generics `new List<String>[10]`, qua cơ chế Type Erasure nó sẽ biến thành `new List[10]`. Lúc này ta có thể gán nó cho `Object[]` và chèn 1 `List<Integer>` vào mảng đó mà Compiler không phát hiện được $\rightarrow$ Gây sụp đổ hệ thống an toàn kiểu (Type Safety) tại Runtime.

---

## ❓ Câu 4: Phân biệt `List`, `List<Object>` và `List<?>`?
* **Mục đích hỏi:** Phân biệt Raw Type, Generic Type và Wildcard Type.
* **Gợi ý trả lời chuẩn:**
  * `List` (Raw type): Không an toàn kiểu, có thể chứa bất kỳ đối tượng nào. Dùng để tương thích code cũ, không khuyên dùng.
  * `List<Object>`: Có an toàn kiểu. Chỉ chấp nhận đúng danh sách được khai báo kiểu `Object`. Không thể gán `List<String>` cho `List<Object>` (do tính Invariant của Generics).
  * `List<?>` (Unbounded Wildcard): Danh sách của một kiểu **chưa xác định**. Có thể gán `List<String>` hay `List<Integer>` cho `List<?>`. Tuy nhiên chỉ được ĐỌC dữ liệu dạng `Object`, KHÔNG được `add()` phần tử mới (trừ `null`).

---
*Tài liệu này giúp bạn làm chủ toàn bộ câu hỏi Generics & Wildcards!*
