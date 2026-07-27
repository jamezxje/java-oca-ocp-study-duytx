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
*Chúc em ôn tập tốt phần String này nhé. Phần này cực kỳ hay ra câu hỏi so sánh `==` (so sánh địa chỉ) và `.equals()` (so sánh giá trị)!*
