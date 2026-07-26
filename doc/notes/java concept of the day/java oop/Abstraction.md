# Abstraction (Tính trừu tượng) trong Java

Chào em! Là Java mentor của em, anh đã biên soạn chi tiết bài học về **Abstraction (Tính trừu tượng)** – một trong 4 cột trụ cốt lõi của Lập trình hướng đối tượng (OOP). Đây là một chủ đề chiếm thời lượng lớn và có độ lừa lọc cực cao trong cả hai kỳ thi OCA và OCP.

Dưới đây là mọi thứ em cần nằm lòng về Abstraction:

---

## 1. Khái niệm Abstraction (Tính trừu tượng)
**Tính trừu tượng** là quá trình ẩn đi các chi tiết triển khai phức tạp bên dưới và chỉ hiển thị các tính năng/giao diện thiết yếu ra bên ngoài. Nó giúp lập trình viên tập trung vào **"đối tượng làm cái gì" (What it does)** thay vì **"đối tượng làm việc đó thế nào" (How it does it)**.

Trong Java, Abstraction được thể hiện qua 2 công cụ chính:
1. **Abstract Class (Lớp trừu tượng):** Đạt độ trừu tượng từ 0% đến 100%.
2. **Interface (Giao diện):** Đạt độ trừu tượng 100% (trước Java 8) và gần 100% (từ Java 8 trở đi nhờ có default/static method).

---

## 2. Abstract Class (Lớp trừu tượng)

Lớp trừu tượng được khai báo bằng từ khóa `abstract`. Nó đóng vai trò là "bản thiết kế khung" cho các lớp con kế thừa.

### Các đặc điểm sống còn của Abstract Class:
* **Không thể khởi tạo trực tiếp:** Em không thể dùng từ khóa `new` để tạo đối tượng từ abstract class.
  ```java
  abstract class Animal {}
  // Animal a = new Animal(); // ❌ LỖI BIÊN DỊCH!
  ```
* **Có thể chứa cả phương thức abstract và non-abstract:**
  * **Abstract method:** Là phương thức được khai báo bằng từ khóa `abstract` và **không có phần thân** `{}` (kết thúc bằng dấu chấm phẩy `;`).
  * **Non-abstract method:** Phương thức bình thường có phần thân.
* **Quy tắc kế thừa:** Lớp con cụ thể (concrete class) kế thừa từ abstract class **bắt buộc phải override** toàn bộ các abstract method của lớp cha, trừ khi chính lớp con đó cũng được khai báo là `abstract`.
* **Vẫn có Constructor:** Dù không thể dùng `new` để tạo đối tượng trực tiếp, abstract class vẫn có constructor để lớp con gọi thông qua `super()` nhằm khởi tạo các thuộc tính của lớp cha.

---

## 3. Interface (Giao diện)

Interface là một bản hợp đồng cam kết. Class nào thực thi (`implements`) một Interface thì bắt buộc phải tuân thủ và triển khai đầy đủ các điều khoản (phương thức) ghi trong Interface đó.

### Các quy tắc cực kỳ nghiêm ngặt của Interface (Trọng tâm OCA):

* **Mọi biến trong Interface** đều mặc định là **`public static final`** (hằng số), dù em có viết tường minh các từ khóa này ra hay không. Do đó, em bắt buộc phải gán giá trị ngay khi khai báo biến.
  ```java
  interface Flyable {
      int SPEED = 10; // Bản chất là: public static final int SPEED = 10;
  }
  ```
* **Mọi phương thức trừu tượng** trong Interface đều mặc định là **`public abstract`**.
* **Từ Java 8 trở đi, Interface có thêm:**
  1. **Default method (từ khóa `default`):** Giúp định nghĩa phương thức có phần thân mặc định trong Interface mà không bắt buộc các class implements nó phải override.
  2. **Static method:** Phương thức tĩnh có phần thân, gọi trực tiếp qua tên Interface.
* **Từ Java 9 trở đi:** Interface hỗ trợ thêm các phương thức **`private`** và **`private static`** dùng để tái sử dụng code nội bộ giữa các default/static method.

---

## 4. So sánh chi tiết: Abstract Class vs Interface

| Đặc điểm | Abstract Class | Interface |
| :--- | :--- | :--- |
| **Kế thừa / Thực thi** | Dùng từ khóa `extends`. Chỉ đơn kế thừa (1 class cha). | Dùng từ khóa `implements`. Có thể thực thi nhiều Interface. |
| **Biến (Variables)** | Có thể khai báo đủ loại biến: instance, static, final, non-final với mọi loại access modifier. | Chỉ có hằng số mặc định là **`public static final`**. |
| **Constructor** | Có Constructor. | **Không** có Constructor. |
| **Phương thức** | Có thể chứa bất kỳ loại phương thức nào (abstract, non-abstract, final, static,...). | Chỉ chứa: abstract method (`public`), default method, static method, và private method (từ Java 9). |
| **Access Modifier** | Đầy đủ: `public`, `protected`, `default`, `private`. | Mặc định mọi abstract method đều là **`public`**. |

---

## 5. Những "Cạm bẫy" OCA/OCP cần lưu ý

### ⚠️ Bẫy 1: Sự kết hợp bất hợp lệ của các Modifier (Illegal Combinations)
* Một phương thức **không thể** vừa là `abstract` vừa là `final` (vì `abstract` bắt buộc phải override, còn `final` thì cấm override).
* Một phương thức **không thể** vừa là `abstract` vừa là `private` (vì lớp con không thể thấy phương thức `private` để mà override).
* Một phương thức **không thể** vừa là `abstract` vừa là `static`.

### ⚠️ Bẫy 2: Xung đột Default Method trong đa thực thi (Diamond Problem)
Nếu một class implements cả 2 Interface có cùng một default method trùng tên và chữ ký (signature), Java sẽ báo lỗi biên dịch vì không biết nên chạy phương thức của Interface nào.
```java
interface A {
    default void hello() { System.out.println("A"); }
}
interface B {
    default void hello() { System.out.println("B"); }
}

// ❌ LỖI BIÊN DỊCH nếu không override tường minh!
class MyClass implements A, B {
    // Sửa bằng cách override và chỉ định rõ muốn gọi từ Interface nào:
    @Override
    public void hello() {
        A.super.hello(); // Gọi hello() của Interface A
    }
}
```

### ⚠️ Bẫy 3: Luật Override và Access Modifier trong Interface
Khi override một phương thức trừu tượng từ Interface, class con **bắt buộc** phải khai báo là `public`. Nếu em quên viết `public` (mặc định để default access modifier), Java sẽ báo lỗi vì hành vi giảm phạm vi truy cập (giảm từ `public` xuống `package-private/default`).

```java
interface Swimmable {
    void swim(); // Bản chất là public abstract void swim();
}

class Penguin implements Swimmable {
    // void swim() {} // ❌ LỖI BIÊN DỊCH: Cannot reduce the visibility of the inherited method!
    
    @Override
    public void swim() {} //  HỢP LỆ!
}
```

---

Anh đã lưu tài liệu này vào thư mục học tập của em rồi nhé. Hãy xem kỹ và thực hành viết code thử để ghi nhớ sâu sắc các quy tắc này. Chúc em học tốt!
