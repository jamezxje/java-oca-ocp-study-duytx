# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 6: Java I/O, Serialization & Design Patterns

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên) về I/O, Serialization và Design Patterns.

---

## ❓ Câu 1: Phân biệt sự khác nhau giữa Classic I/O và NIO.2? Tại sao đọc file lớn nên ưu tiên dùng `Files.lines()` hơn `Files.readAllLines()`?
* **Mục đích hỏi:** Đánh giá độ sâu về quản lý bộ nhớ Heap khi xử lý tập tin lớn.
* **Gợi ý trả lời chuẩn:**
  * **Classic I/O** xử lý dạng Blocking theo luồng byte/kí tự (`InputStream`). **NIO.2** xử lý dạng Non-blocking dựa trên Buffer/Channel và API `Path`/`Files`.
  * `Files.readAllLines(path)` đọc **TOÀN BỘ** các dòng của file và nạp tất cả vào bộ nhớ Heap cùng một lúc dưới dạng `List<String>`. Nếu file dung lượng lớn vài GB, ứng dụng sẽ bị ngắt ngừ bởi lỗi **`OutOfMemoryError`**.
  * `Files.lines(path)` trả về một `Stream<String>` xử lý **Lazy Evaluation** (đọc từng dòng một theo nhu cầu). Bộ nhớ Heap luôn duy trì ở mức cực thấp bất kể file dung lượng hàng chục GB.

---

## ❓ Câu 2: Từ khóa `transient` trong Java có tác dụng gì? Điều gì xảy ra nếu một Class implement `Serializable` nhưng chứa thuộc tính của một Class KHÔNG implement `Serializable`?
* **Mục đích hỏi:** Kiểm tra các quy tắc bẫy thi OCP về Serialization.
* **Gợi ý trả lời chuẩn:**
  * Từ khóa `transient` đánh dấu một thuộc tính **KHÔNG bị tuần tự hóa (Serialize)** xuống đĩa hay qua mạng. Khi deserialize, thuộc tính này nhận giá trị mặc định (`null`, `0` hoặc `false`). Thường dùng bảo vệ dữ liệu nhạy cảm (như mật khẩu).
  * Nếu một Class `Serializable` chứa thuộc tính của một Class không `Serializable`, JVM sẽ ném ra ngoại lệ **`NotSerializableException`** tại thời điểm chạy (Runtime) khi thực hiện Serialize đối tượng đó.

---

## ❓ Câu 3: `serialVersionUID` dùng để làm gì? Chuyện gì xảy ra nếu ta không khai báo `serialVersionUID` tường minh và sau đó sửa đổi cấu trúc Class?
* **Mục đích hỏi:** Đánh giá sự hiểu biết về tính tương thích phiên bản đối tượng.
* **Gợi ý trả lời chuẩn:**
  * `serialVersionUID` là mã định danh phiên bản của Class dùng để xác minh tính tương thích giữa đối tượng bị serialize và Class hiện tại khi deserialize.
  * Nếu ta không khai báo tường minh, JVM sẽ tự động tính toán một `serialVersionUID` dựa trên cấu trúc các thuộc tính và phương thức của Class.
  * **Hậu quả:** Nếu sau đó ta thêm/sửa 1 thuộc tính trong Class, JVM sẽ tự sinh ra 1 `serialVersionUID` mới $\rightarrow$ Khi deserialize file dữ liệu cũ, JVM thấy 2 ID không trùng nhau và ném ra **`InvalidClassException`**, khiến dữ liệu cũ không thể đọc lại được!

---

## ❓ Câu 4: Hãy viết một Class Singleton chuẩn Thread-safe và Lazy Loading trong Java mà KHÔNG dùng từ khóa `synchronized`?
* **Mục đích hỏi:** Kiểm tra kiến thức thiết kế Pattern kết hợp với cơ chế Class Loading của JVM.
* **Gợi ý trả lời chuẩn:**
  * Sử dụng giải pháp **Bill Pugh Singleton (Initialization-on-demand holder idiom)**:
  ```java
  public class Singleton {
      private Singleton() {}

      private static class Holder {
          private static final Singleton INSTANCE = new Singleton();
      }

      public static Singleton getInstance() {
          return Holder.INSTANCE;
      }
  }
  ```
  * **Giải thích:** Lớp nội `Holder` chỉ được JVM nạp vào bộ nhớ khi phương thức `getInstance()` được gọi lần đầu tiên (Lazy Loading). Việc khởi tạo hằng số `static final INSTANCE` do JVM đảm bảo tính nguyên tử và Thread-safe mà không tốn chi phí khóa `synchronized`.

---
*Tài liệu này hoàn tất trọn bộ 6 Chặng Câu Hỏi Phỏng Vấn Java Core!*
