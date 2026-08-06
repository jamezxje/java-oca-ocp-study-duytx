# Exception Handling (Xử Lý Ngoại Lệ) Trong Java

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Java Exception Hierarchy, Quy tắc Try-Catch-Finally, Try-With-Resources và Các bẫy đề thi OCA/OCP & Phỏng vấn**.

---

## Phần 1: Cấu Trúc Cây Kế Thừa Exception (Hierarchy)

Tất cả các lỗi và ngoại lệ trong Java đều là đối tượng kế thừa từ lớp cha **`java.lang.Throwable`**.

```
                           java.lang.Throwable
                                    │
           ┌────────────────────────┴────────────────────────┐
           ▼                                                 ▼
   java.lang.Error                                  java.lang.Exception
 (Unchecked - Lỗi hệ thống)                                  │
  - OutOfMemoryError                        ┌────────────────┴────────────────┐
  - StackOverflowError                      ▼                                 ▼
                                   RuntimeException                Checked Exceptions
                                (Unchecked - Lỗi logic)         (Bắt buộc phải xử lý)
                                 - NullPointerException           - IOException
                                 - ArithmeticException            - SQLException
                                 - ArrayIndexOutOfBoundsException - ClassNotFoundException
```

---

### 1. Phân biệt Checked Exception vs Unchecked Exception

| Tiêu chí | Checked Exception | Unchecked Exception (RuntimeException) | Error |
| :--- | :--- | :--- | :--- |
| **Lớp cha** | Kế thừa `Exception` (không thuộc `RuntimeException`). | Kế thừa `java.lang.RuntimeException`. | Kế thừa `java.lang.Error`. |
| **Thời điểm kiểm tra** | **Compile-time** (Trình biên dịch bắt buộc phải xử lý). | **Runtime** (Không bắt buộc xử lý khi compile). | **Runtime** (Cấp hệ thống/JVM). |
| **Nguyên nhân** | Do yếu tố bên ngoài hệ thống (File không tồn tại, mất kết nối DB, đứt mạng). | Do lỗi lập trình viên (Truy cập `null`, chia cho 0, vượt chỉ số mảng). | Do sự cố nghiêm trọng của JVM (Hết bộ nhớ Heap, tràn Stack). |
| **Cơ chế xử lý** | Bắt buộc phải bọc `try-catch` hoặc khai báo `throws` ở chữ ký hàm. | Không bắt buộc khai báo `throws`, nên sửa bằng cách check logic code. | Không nên try-catch vì JVM không thể tự phục hồi. |

---

## Phần 2: Luồng Thực Thi Try-Catch-Finally & Cạm Bẫy

### 1. Luồng chạy của `finally` block
* Block `finally` **LUÔN LUÔN ĐƯỢC THỰC THI**, bất kể có Exception xảy ra hay không, và bất kể `try` hay `catch` có lệnh `return`.

#### ⚠️ Cạm bẫy 1: Dùng lệnh `return` trong block `finally` (Đừng bao giờ làm!)
Nếu block `finally` chứa câu lệnh `return`, nó sẽ **ghi đè và nuốt chửng** kết quả trả về cũng như ngoại lệ bị ném ra từ block `try` hoặc `catch`!

```java
public int test() {
    try {
        return 10;
    } catch (Exception e) {
        return 20;
    } finally {
        return 30; // ❌ CẠM BẪY: Hàm này sẽ LUÔN trả về 30!
    }
}
```

#### ⚠️ Cạm bẫy 2: Trường hợp DUY NHẤT block `finally` KHÔNG chạy
Block `finally` sẽ bị bỏ qua nếu và chỉ nếu:
1. Phương thức **`System.exit(0)`** được gọi trước đó.
2. Tiến trình JVM bị ngắt đột ngột (Cúp điện, bị `kill -9` tín hiệu OS, hoặc JVM bị Crash).

---

### 2. Cú pháp Multi-Catch (Java 7+)

Cho phép bắt nhiều Exception trên cùng 1 block `catch`:
```java
try {
    // Thao tác I/O và SQL
} catch (IOException | SQLException e) { // Dùng toán tử pipe |
    logger.error("Lỗi thao tác dữ liệu", e);
}
```
* **Ràng buộc:** Các Exception trong cùng 1 multi-catch **không được phép có quan hệ cha-con** với nhau (Ví dụ `catch (FileNotFoundException | IOException e)` là **LỖI BIÊN DỊCH** vì `FileNotFoundException` là con của `IOException`).
* Biến `e` trong multi-catch mặc định là **`implicitly final`** (không thể gán lại `e = new Exception()`).

---

## Phần 3: `try-with-resources` & Lớp `AutoCloseable` (Java 7+)

Dùng để tự động đóng tài nguyên (File stream, DB Connection, Socket) sau khi dùng xong mà không cần viết `resource.close()` thủ công trong `finally`.

```java
// Tài nguyên khai báo trong try() sẽ tự động được đóng theo thứ tự NGƯỢC LẠI
try (BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
     Connection conn = DriverManager.getConnection(url)) {
    // Đọc dữ liệu
} catch (IOException | SQLException e) {
    // Xử lý lỗi
}
// Cả reader và conn tự động được đóng tại đây!
```

### 1. Điều kiện để dùng với `try-with-resources`
* Tài nguyên khai báo trong `try(...)` **bắt buộc phải implements interface `java.lang.AutoCloseable`** hoặc `java.io.Closeable`.

### 2. Khái niệm Suppressed Exceptions (Ngoại lệ bị đè)
Nếu block `try` ném ra Exception A, và khi đóng tài nguyên hàm `close()` lại ném tiếp Exception B:
* Exception A sẽ được ưu tiên ném ra ngoài.
* Exception B sẽ được nạp vào danh sách **`e.getSuppressed()`** của Exception A chứ không bị nuốt mất!

---

## Phần 4: Phân biệt `throw` vs `throws`

| Tiêu chí | Từ khóa `throw` | Từ khóa `throws` |
| :--- | :--- | :--- |
| **Mục đích** | Chủ động ném ra một đối tượng ngoại lệ cụ thể. | Khai báo các ngoại lệ mà phương thức **có thể ném ra**. |
| **Vị trí** | Nằm bên trong thân phương thức (`throw new Exception()`). | Nằm ở chữ ký của phương thức (`public void run() throws IOException`). |
| **Số lượng** | Chỉ ném 1 đối tượng ngoại lệ tại 1 thời điểm. | Có thể khai báo nhiều Exception phân cách bằng dấu phẩy. |

---

## 📌 Cheat Sheet Bẫy Thi OCA/OCP & Phỏng Vấn Về Exception

1. **Bẫy Override Phương Thức Có `throws`:**
   * Lớp con khi override phương thức của lớp cha **KHÔNG ĐƯỢC PHÉP** ném ra Checked Exception mới hoặc rộng hơn Checked Exception của lớp cha.
   * Lớp con **được phép** ném ra Checked Exception nhỏ hơn (con), hoặc không ném gì cả, hoặc ném thêm Unchecked Exception tùy ý.
2. **Anti-pattern Swallowing Exception (Nuốt ngoại lệ):**
   * Bắt exception bằng `catch (Exception e) {}` mà không ghi log hay rethrow là sai nghiêm trọng trong dự án thực tế.
3. **Không bao giờ `catch (Throwable e)`:**
   * Vì `Throwable` bao gồm cả `Error` (như `OutOfMemoryError`). Bắt `Error` khiến ứng dụng cố chạy tiếp trong trạng thái JVM đã bị hỏng.

---
*Tài liệu này hoàn chỉnh toàn bộ lý thuyết Exception Handling!*
