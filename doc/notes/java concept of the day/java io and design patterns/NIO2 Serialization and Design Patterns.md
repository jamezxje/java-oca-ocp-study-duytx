# Java I/O, NIO.2, Serialization & Best Design Patterns

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Java I/O vs NIO.2, Cơ chế Serialization và 5 Design Patterns cốt lõi** dành cho lập trình viên Java 2 năm kinh nghiệm chuẩn bị phỏng vấn và thi chứng chỉ OCP.

---

## Phần 1: Java I/O vs NIO.2 (New I/O)

### 1. Phân biệt Java I/O (Classic IO) và NIO.2 (Java 7+)

| Tiêu chí | Classic I/O (`java.io`) | NIO.2 (`java.nio`) |
| :--- | :--- | :--- |
| **Mô hình xử lý** | Dựa trên **Stream (Dòng)** (`InputStream`, `OutputStream`). | Dựa trên **Buffer (Bộ đệm)** và **Channel (Kênh)**. |
| **Chế độ thực thi** | **Blocking I/O** (Luồng bị nghẽn chờ đọc/ghi xong). | **Non-blocking I/O** (Không bị nghẽn luồng nhờ Selector). |
| **Xử lý đường dẫn** | Dùng lớp `File` (khó xử lý symbolic link, thiếu API tiện ích). | Dùng interface `Path`, lớp tiện ích `Files` và `FileSystems`. |
| **Hiệu năng** | Chậm hơn đối với ứng dụng đọc/ghi file lớn hoặc hàng ngàn socket connection. | Cực kỳ nhanh (Tận dụng cơ chế `Zero-Copy` của OS). |

---

### 2. Các Lớp Cốt Lõi Trong NIO.2 (`Path` và `Files`)

NIO.2 cung cấp 2 thành phần chính xử lý tập tin cực kỳ mạnh mẽ:

* **`Path`**: Đại diện cho đường dẫn tập tin/thư mục trong hệ thống.
  ```java
  Path path = Paths.get("document/notes/Welcome.md");
  ```
* **`Files`**: Lớp tiện ích tĩnh chứa hàng loạt phương thức đọc/ghi file gọn gàng:
  ```java
  // Đọc toàn bộ nội dung file ngắn thành String
  String content = Files.readString(path);

  // Đọc toàn bộ các dòng file thành List<String>
  List<String> lines = Files.readAllLines(path);

  // Đọc file dưới dạng Stream (Hiệu năng cao cho file cực lớn)
  try (Stream<String> linesStream = Files.lines(path)) {
      linesStream.filter(line -> line.contains("OCA")).forEach(System.out.println);
  }
  ```

---

## Phần 2: Cơ Chế Serialization (Tuần Tự Hóa Đối Tượng)

**Serialization** là quá trình chuyển đổi trạng thái của một đối tượng Java thành một chuỗi byte để lưu xuống đĩa hoặc truyền qua mạng. **Deserialization** là quá trình ngược lại.

---

### 1. Các Quy Tắc Sống Còn Của Serialization (Trọng Tâm OCP)

1. **Interface `Serializable`:** Class muốn serialize **bắt buộc** phải implements marker interface `java.io.Serializable` (Interface này rỗng, không có phương thức).
2. **Từ khóa `transient`:** Bất kỳ thuộc tính nào được khai báo với từ khóa `transient` sẽ **KHÔNG bị serialize** (giá trị sẽ trở thành `null` hoặc `0` khi deserialize). Thường dùng cho thông tin nhạy cảm (password, credit card) hoặc các biến tạm.
3. **Biến `static`:** Biến `static` thuộc về Class chứ không thuộc về Object, do đó biến `static` **KHÔNG bị serialize**.
4. **Lỗi `NotSerializableException`:** Nếu một Class implements `Serializable` nhưng lại chứa một thuộc tính là một Object của một Class **KHÔNG** implements `Serializable`, JVM sẽ ném ra `NotSerializableException` tại Runtime!

---

### 2. Ý Nghĩa Của `serialVersionUID`

```java
private static final long serialVersionUID = 1L;
```
* **Mục đích:** Là một con số định danh phiên bản của Class dùng trong quá trình Deserialization.
* **Cơ chế:** Khi deserialize, JVM sẽ so sánh `serialVersionUID` của chuỗi byte truyền vào với `serialVersionUID` của Class hiện tại trong ứng dụng.
* **Hậu quả nếu không trùng nhau:** JVM sẽ ném ra ngoại lệ **`InvalidClassException`** và từ chối nạp đối tượng!

---

## Phần 3: Top 5 Design Patterns Phổ Biến Nhất Trong Java Core

Nhà tuyển dụng rất thích hỏi về cách bạn áp dụng Design Patterns vào code Java thực tế.

---

### 1. Singleton Pattern (Chỉ tạo duy nhất 1 đối tượng)

#### Cách viết Thread-safe tối ưu (Bill Pugh Singleton / Initialization-on-demand holder):
```java
public class DatabaseConnection {
    private DatabaseConnection() {} // Private Constructor chống new từ bên ngoài

    // Inner static class chỉ được nạp khi gọi getInstance() (Lazy Loading)
    private static class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }
}
```

---

### 2. Builder Pattern (Tạo đối tượng phức tạp nhiều thuộc tính)

* **Ứng dụng:** Tránh tình trạng Constructor quá nhiều tham số (Telescoping Constructor).

```java
User user = new User.Builder("Alice", "alice@email.com")
    .age(25)
    .phone("0901234567")
    .address("Hanoi")
    .build();
```

---

### 3. Factory Method Pattern (Khởi tạo đối tượng theo loại)

* **Ứng dụng:** Che giấu logic khởi tạo đối tượng cụ thể đằng sau một phương thức Factory.
* **Ví dụ trong Java SDK:** `Executors.newFixedThreadPool(10)`, `List.of("A", "B")`.

---

### 4. Strategy Pattern (Thuật toán linh hoạt)

* **Ứng dụng:** Cho phép chọn thuật toán tại thời điểm chạy (Runtime) thông qua Interface.
* **Ví dụ trong Java SDK:** Phân loại sắp xếp bằng `Comparator<T>`.

---

### 5. Observer Pattern (Đăng ký & Phát sự kiện)

* **Ứng dụng:** Một đối tượng thay đổi trạng thái sẽ tự động thông báo cho tất cả các đối tượng đăng ký theo dõi nó.
* **Ví dụ thực tế:** Event Listener trong GUI, Spring ApplicationEventPublisher.

---

## 📌 Summary Sheet Chặng 6

1. **Classic I/O vs NIO.2:** Classic IO (Blocking, Stream); NIO.2 (Non-blocking, Buffer/Channel, API `Path`/`Files` cực mạnh).
2. **Serialization Rules:** Must implement `Serializable`; `transient` và `static` không bị serialize; `serialVersionUID` đảm bảo tương thích phiên bản (ngừa `InvalidClassException`).
3. **5 Core Patterns:**
   * **Singleton:** Bill Pugh Holder (Thread-safe, Lazy Loading).
   * **Builder:** Khởi tạo object nhiều param sạch sẽ.
   * **Factory:** Che giấu logic `new` object đằng sau hàm tạo.
   * **Strategy:** Đổi thuật toán linh hoạt (như `Comparator`).
   * **Observer:** Event/Listener notification.

---
*Tài liệu này hoàn tất 6 chặng lý thuyết Java Core nâng cao!*
