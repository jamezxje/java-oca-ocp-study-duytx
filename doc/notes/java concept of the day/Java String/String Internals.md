# Cơ chế lưu trữ String trong Java

Chào em! String trong Java là một chủ đề không chỉ xuất hiện trong kỳ thi OCA/OCP mà còn cực kỳ quan trọng trong thực tế lập trình để tối ưu bộ nhớ. Dưới đây là những điều em cần nắm vững:

---

## 1. String Pool (Vùng chứa String)
Để tối ưu hóa bộ nhớ, Java sử dụng một vùng nhớ đặc biệt trong **Heap** gọi là **String Pool**.
* **Nguyên tắc:** Nếu em tạo một String bằng cách dùng **String literal** (ví dụ: `String s = "Hello";`), Java sẽ kiểm tra trong String Pool xem đã có đối tượng String có giá trị `"Hello"` chưa.
  * **Nếu có:** Nó trả về địa chỉ của đối tượng đã tồn tại trong Pool.
  * **Nếu chưa:** Nó tạo mới đối tượng trong Pool và trả về địa chỉ đó.
* **Mục đích:** Tránh tạo thừa đối tượng giống hệt nhau, giúp tiết kiệm bộ nhớ đáng kể.

---

## 2. Literals vs `new` Keyword

| Cách tạo | Cơ chế lưu trữ |
| :--- | :--- |
| **String Literal** (`String s = "Hello";`) | Tạo (hoặc dùng lại) đối tượng trong **String Pool**. |
| **`new` Keyword** (`String s = new String("Hello");`) | **Luôn luôn** tạo đối tượng mới nằm trong vùng nhớ **Heap** thông thường (ngoài Pool). |

### Ví dụ minh họa:
```java
String s1 = "Java"; // Nằm trong Pool
String s2 = "Java"; // s1 == s2 (Cùng trỏ vào Pool)

String s3 = new String("Java"); // Nằm trong Heap ngoài Pool
// s1 == s3 là FALSE (vì địa chỉ khác nhau)
// s1.equals(s3) là TRUE (vì so sánh giá trị)
```

---

## 3. String là Bất biến (Immutability)
* **Khái niệm:** Một khi đối tượng `String` đã được tạo ra, nội dung của nó **không bao giờ thay đổi được**.
* **Tại sao?**
  1. Bảo mật: String được dùng để lưu trữ dữ liệu nhạy cảm (username, password, file path). Nếu nó thay đổi, các rủi ro bảo mật sẽ rất lớn.
  2. An toàn cho String Pool: Nếu một String thay đổi, tất cả các biến đang trỏ vào cùng đối tượng đó trong Pool sẽ bị thay đổi theo, gây sai sót dữ liệu.
* **Khi em thực hiện:** `s = s + " World";`, Java không hề thay đổi chuỗi `"Hello"` ban đầu. Nó tạo ra một đối tượng `String` mới là `"Hello World"` và làm cho biến `s` trỏ vào địa chỉ của đối tượng mới đó. Đối tượng cũ sẽ bị Garbage Collector (GC) thu dọn.

---

## 4. Phương thức `.intern()`
* Khi em dùng `String s = new String("Hello");`, đối tượng nằm ngoài Pool.
* Phương thức **`s.intern()`** giúp em ép JVM kiểm tra giá trị của `s` trong String Pool:
  * Nếu đã có trong Pool, nó trả về địa chỉ trong Pool.
  * Nếu chưa có, nó đưa đối tượng đó vào Pool và trả về địa chỉ trong Pool.
```java
String s1 = new String("Hello");
String s2 = s1.intern();
String s3 = "Hello";
// s2 == s3 là TRUE (vì giờ cả hai đều trỏ vào String Pool)
```

---

## 5. Câu hỏi thường gặp (String FAQs)

*   **Tại sao String lại là bất biến (immutable)?**
    *   Để bảo mật (security): Các thông số như username/password không thể bị thay đổi.
    *   Để dùng String Pool: Nếu String thay đổi, tất cả các tham chiếu tới nó sẽ bị ảnh hưởng, gây lỗi dữ liệu.
    *   Tối ưu hóa bộ nhớ: Giúp JVM sử dụng lại các String trong Pool an toàn.

*   **Sự khác biệt giữa String, StringBuffer và StringBuilder?**
    *   `String`: Bất biến. Mọi thao tác sửa đổi đều tạo ra đối tượng mới.
    *   `StringBuffer`: Có thể thay đổi nội dung (mutable), an toàn cho đa luồng (thread-safe, synchronized).
    *   `StringBuilder`: Có thể thay đổi nội dung (mutable), không an toàn cho đa luồng (not thread-safe), nhưng chạy nhanh hơn `StringBuffer`.

*   **String có thể kế thừa (extends) không?**
    *   **KHÔNG!** `String` là một class được khai báo với từ khóa `final`. Do đó, nó không thể bị kế thừa.

*   **So sánh `==` và `.equals()`?**
    *   `==`: So sánh địa chỉ bộ nhớ (reference).
    *   `.equals()`: So sánh giá trị nội dung của String (đã được override từ lớp `Object`).

---

## 6. Các Bẫy OCA/OCP "Tử thần" về String

Đây là phần mà các đề thi cực kỳ thích dùng để "bẫy" học viên. Em hãy đọc kỹ và hiểu bản chất:

### ⚠️ Bẫy 1: Compiler Optimization (Tối ưu hóa của trình biên dịch)
Java Compiler cực kỳ thông minh. Nếu em cộng các **String Literal** trực tiếp, nó sẽ tự tính toán ngay tại thời điểm biên dịch.
```java
String s1 = "AB";
String s2 = "A" + "B"; 
// Kết quả: s1 == s2 là TRUE! 
// Vì Compiler tối ưu "A" + "B" thành "AB" ngay trong String Pool.
```
**Nhưng:** Nếu em cộng với **biến**, nó sẽ tạo ra đối tượng mới trên Heap!
```java
String s1 = "A";
String s2 = s1 + "B"; // Lúc này s2 được tạo trên HEAP (không nằm trong Pool)
String s3 = "AB";     // Nằm trong Pool
// s2 == s3 là FALSE!
```

### ⚠️ Bẫy 2: String Bất biến (Immutability)
Nhiều bạn lầm tưởng `s.toUpperCase()` sẽ thay đổi chuỗi `s` ban đầu.
```java
String s = "java";
s.toUpperCase(); 
System.out.println(s); // KẾT QUẢ: "java" (Vẫn là chuỗi cũ!)
// Phải dùng: s = s.toUpperCase();
```
*Ghi nhớ:* Mọi phương thức thao tác trên String như `substring()`, `concat()`, `replace()`, `toUpperCase()` đều **trả về một String mới**, hoàn toàn không ảnh hưởng đến String cũ.

### ⚠️ Bẫy 3: So sánh `==` với chuỗi tạo từ `new` và biến
Đây là sự kết hợp của Bẫy 1 và kiến thức về Heap.
```java
String s1 = new String("Java");
String s2 = "Java";
String s3 = "Ja" + "va"; // Tối ưu thành "Java" trong Pool
// s1 == s2 -> FALSE
// s2 == s3 -> TRUE
```

### ⚠️ Bẫy 4: Truyền String vào phương thức
Vì `String` là bất biến, khi em truyền String vào một phương thức và cố thay đổi nó, bản gốc ở ngoài phương thức đó sẽ **không bị thay đổi**.
```java
void change(String s) {
    s = s.concat(" World");
}
// ...
String str = "Hello";
change(str);
System.out.println(str); // KẾT QUẢ: "Hello"
```

---

## 7. StringBuilder và StringBuffer (Giải pháp Mutable)

Do `String` là **Bất biến (Immutable)**, mọi thao tác nối chuỗi trong vòng lặp sẽ tạo ra rất nhiều đối tượng trung gian, gây lãng phí bộ nhớ. `StringBuilder` và `StringBuffer` ra đời để giải quyết vấn đề này.

### Tại sao cần chúng?
* `String` = Bất biến (Nối chuỗi tạo ra đối tượng mới).
* `StringBuilder`/`StringBuffer` = Có thể thay đổi (Mutable - sửa trực tiếp trên vùng nhớ cũ, không tạo đối tượng mới).

### So sánh StringBuffer vs StringBuilder

| Đặc điểm | StringBuffer | StringBuilder |
| :--- | :--- | :--- |
| **Ra đời** | Java 1.0 (Cũ) | Java 5 (Mới hơn) |
| **Thread-safety** | **Có** (Các phương thức đều `synchronized`). | **Không** (Không đồng bộ). |
| **Hiệu năng** | Chậm hơn (do tốn chi phí đồng bộ). | **Nhanh hơn** (khuyên dùng trong hầu hết trường hợp). |

### Các lưu ý cho kỳ thi OCA/OCP:
1.  **Cả hai đều không kế thừa từ `String`**: Cả ba (String, StringBuilder, StringBuffer) đều không có quan hệ kế thừa với nhau.
2.  **Sử dụng `append()`**: Thay vì dùng `+` hoặc `concat()`, hãy dùng `append()` của `StringBuilder`.
3.  **Bẫy Overloading**: `StringBuilder` và `StringBuffer` có các phương thức Overloading giống hệt nhau (`append`, `insert`, `delete`). Hãy cẩn thận khi đọc đề!
4.  **Không dùng `==`**: Cả `StringBuilder` và `StringBuffer` **KHÔNG** override phương thức `.equals()` từ lớp `Object`. Vì vậy, nếu em dùng `.equals()` để so sánh nội dung của hai `StringBuilder`, nó sẽ so sánh **địa chỉ bộ nhớ** (giống `==`) và kết quả sẽ là `false` ngay cả khi nội dung giống nhau.
    *   *Cách sửa:* Phải chuyển sang String trước khi so sánh: `sb1.toString().equals(sb2.toString())`.

---

*Chúc em ôn tập thật kỹ! Những cái bẫy này chính là chìa khóa để phân biệt học viên giỏi và học viên xuất sắc trong kỳ thi OCA/OCP!*
