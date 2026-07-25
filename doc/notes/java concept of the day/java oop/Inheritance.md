# Inheritance (Kế thừa) trong Java

Chào em! Với tư cách là Java mentor, anh đã tổng hợp các khái niệm then chốt về Inheritance để em ôn tập cho kỳ thi. Inheritance là nền tảng của OOP, giúp tạo ra các mối quan hệ "is-a" (là một) giữa các class.

---

## 1. Bản chất (Core Concept)
* **Từ khóa:** `extends`.
* **Mối quan hệ:** Quan hệ "Is-a" (Ví dụ: `Dog` is a `Animal`).
* **Tính chất:** Java chỉ hỗ trợ **đơn kế thừa (Single Inheritance)** đối với Class (1 Class chỉ có tối đa 1 cha trực tiếp), nhưng hỗ trợ **đa kế thừa** thông qua Interface.

---

## 2. Thứ tự khởi tạo (Initialization Order)
Khi khởi tạo một đối tượng lớp con:
1. Load class cha, load class con (Static blocks/vars).
2. Gọi `super()` (Constructor của cha).
3. Khởi tạo Instance Variables & IIB của cha.
4. Chạy Constructor của cha.
5. Khởi tạo Instance Variables & IIB của con.
6. Chạy Constructor của con.

---

## 3. Các quy tắc quan trọng (Trọng tâm thi)

### A. Access Modifier trong kế thừa
* **Không được phép giảm phạm vi truy cập** khi Override phương thức. (Ví dụ: Cha là `protected`, con không được phép override thành `private` hoặc `default`).
* **Được phép mở rộng phạm vi truy cập** (Ví dụ: Cha là `protected`, con có thể override thành `public`).

### B. Phương thức không thể bị kế thừa (Không thể override)
* Các phương thức có từ khóa `private` (không được kế thừa, chỉ có thể tạo phương thức mới trùng tên).
* Các phương thức `final`.
* Các phương thức `static` (không phải override, mà là **hiding** - che dấu).

### C. Quy tắc gọi `super()`
* Lệnh đầu tiên trong Constructor của lớp con **bắt buộc** phải là `super(...)` hoặc `this(...)`.
* Nếu không viết, trình biên dịch tự động thêm `super()` (gọi constructor không tham số của lớp cha). Nếu lớp cha không có constructor không tham số, em **phải** tự gọi `super(tham_số)` tường minh.

---

## 4. Bẫy kinh điển (Common Pitfalls)

### ⚠️ Bẫy 1: Phương thức Static không thể Override (Method Hiding)
Phương thức `static` không tuân theo đa hình (polymorphism). Nếu lớp con định nghĩa lại phương thức `static` giống hệt lớp cha, đó là **Method Hiding**, không phải Override.
```java
class Parent {
    public static void talk() { System.out.println("Parent"); }
}
class Child extends Parent {
    public static void talk() { System.out.println("Child"); }
}

Parent p = new Child();
p.talk(); // Kết quả: In ra "Parent" (Dựa vào kiểu tham chiếu, không phải kiểu đối tượng)
```

### ⚠️ Bẫy 2: Biến Instance không bị Override
Trong Java, chỉ **phương thức** bị override, **biến** thì không. Nếu con khai báo lại biến trùng tên với cha, biến của con sẽ che khuất (shadowing) biến của cha.
```java
class Parent { int x = 10; }
class Child extends Parent { int x = 20; }

Parent p = new Child();
System.out.println(p.x); // Kết quả: 10 (Truy cập biến của cha)
```

---

## 5. Từ khóa `final`
* Class có từ khóa `final`: **Không thể bị kế thừa**.
* Phương thức có từ khóa `final`: **Không thể bị override**.

---
*Cố gắng nắm vững các quy tắc này nhé! Nếu cần anh ra đề bài tập về "Kế thừa đa hình" để test kiến thức, cứ bảo anh!*
