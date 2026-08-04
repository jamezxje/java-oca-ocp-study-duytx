# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 5: Memory Management & Garbage Collection

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên) về Quản Lý Bộ Nhớ và GC.

---

## ❓ Câu 1: Tại sao Java có tiến trình Garbage Collection tự động mà ứng dụng vẫn bị rò rỉ bộ nhớ (Memory Leak)? Kể tên 3 nguyên nhân gây Memory Leak thực tế mà bạn từng gặp?
* **Mục đích hỏi:** Đánh giá kinh nghiệm thực chiến và khả năng chẩn đoán sự cố ứng dụng Production.
* **Gợi ý trả lời chuẩn:**
  * GC chỉ thu gom các đối tượng **bị mồ côi (không còn bất kỳ tham chiếu nào trỏ tới)**. Nêu ứng dụng không dùng đến đối tượng nữa nhưng vẫn vô tình giữ biến tham chiếu trỏ tới nó, GC sẽ không bao giờ thu gom được $\rightarrow$ Trực tiếp gây Memory Leak.
  * **3 nguyên nhân thực tế:**
    1. **Biến `static` lưu bộ sưu tập (Collection):** Thêm dữ liệu vào `static List` hoặc `static Map` mà không bao giờ xóa bớt đi.
    2. **Quên đóng I/O Stream hoặc Database Connections:** Không dùng `try-with-resources`.
    3. **Dùng Mutable Object làm Key trong `HashMap` mà không override `equals/hashCode`:** Khiến dữ liệu bị kẹt ở Bucket cũ vĩnh viễn.

---

## ❓ Câu 2: Sự khác biệt giữa `StackOverflowError` và `OutOfMemoryError` là gì? Cách điều chỉnh kích thước bộ nhớ qua JVM Flags?
* **Mục đích hỏi:** Kiểm tra sự hiểu biết về mô hình bộ nhớ JVM và kỹ năng cấu hình JVM.
* **Gợi ý trả lời chuẩn:**
  * **`StackOverflowError`:** Xảy ra ở bộ nhớ **Stack** khi danh sách các Stack Frame gọi phương thức vượt quá dung lượng Stack (thường do đệ quy vô hạn hoặc gọi hàm lồng nhau quá sâu). Điều chỉnh qua cờ JVM: **`-Xss`** (ví dụ: `-Xss1m`).
  * **`OutOfMemoryError` (OOM):** Xảy ra ở bộ nhớ **Heap** hoặc **Metaspace** khi JVM không thể cấp phát thêm bộ nhớ cho đối tượng mới dù GC đã chạy hết công suất. Điều chỉnh qua cờ JVM: **`-Xms`** (Initial Heap), **`-Xmx`** (Max Heap), **`-XX:MaxMetaspaceSize`**.

---

## ❓ Câu 3: Java là Pass-by-Value hay Pass-by-Reference? Hãy chứng minh bằng một ví dụ code nhỏ?
* **Mục đích hỏi:** Đây là câu hỏi kinh điển để kiểm tra bản chất truyền tham số trong Java.
* **Gợi ý trả lời chuẩn:**
  * **Java LUÔN LUÔN là Pass-by-Value (Truyền theo giá trị)!**
  * Với kiểu nguyên thủy (Primitive), Java truyền **bản sao giá trị** của biến.
  * Với kiểu đối tượng (Object), Java truyền **bản sao của giá trị tham chiếu (copy of the reference value)** trỏ tới địa chỉ bộ nhớ trên Heap.
  * **Ví dụ chứng minh:**
    ```java
    public static void swap(Student a, Student b) {
        Student temp = a;
        a = b;
        b = temp;
    }
    // Ở bên ngoài hàm swap(), hai biến s1 và s2 gốc KHÔNG HỀ BỊ ĐỔI VỊ TRÍ! 
    // Vì hàm swap chỉ đổi giá trị của bản sao địa chỉ bộ nhớ trong Stack Frame cục bộ.
    ```

---

## ❓ Câu 4: Phân biệt `WeakReference` và `SoftReference`? Ứng dụng thực tế của chúng là gì?
* **Mục đích hỏi:** Đánh giá độ sâu về kỹ thuật Caching và bộ nhớ trong Java.
* **Gợi ý trả lời chuẩn:**
  * **`SoftReference`:** Đối tượng chỉ bị GC thu gom khi bộ nhớ Heap **bị thiếu hụt nghiêm trọng (sắp bị OOM)**. Dùng làm bộ nhớ đệm Cache nhạy cảm với bộ nhớ (Memory-sensitive Cache).
  * **`WeakReference`:** Đối tượng sẽ bị GC thu gom **ngay lập tức trong đợt GC tiếp theo**, bất kể bộ nhớ còn nhiều hay ít. Dùng trong `WeakHashMap` hoặc để tránh rò rỉ bộ nhớ trong `ThreadLocal`.

---
*Tài liệu này giúp bạn làm chủ toàn bộ câu hỏi JVM Memory & GC!*
