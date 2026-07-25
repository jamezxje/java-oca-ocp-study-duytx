# Từ khóa 'this' và 'super' trong Java

Chào em! Là Java mentor của em, anh đã tổng hợp kiến thức về `this` và `super` vào file này. Đây là hai từ khóa xuất hiện cực kỳ nhiều trong các câu hỏi về Constructor và tính kế thừa. Nắm vững chúng giúp em xử lý gọn gàng các câu hỏi "bẫy" trong kỳ thi.

---

## 1. Từ khóa `this`
`this` là một tham chiếu (reference) đến **đối tượng hiện tại** (đối tượng đang gọi phương thức hoặc constructor).

* **Công dụng 1: Phân biệt biến instance và tham số truyền vào**
  Dùng khi tên tham số trùng tên với tên biến instance của class.
  ```java
  public void setName(String name) {
      this.name = name; // 'this.name' là biến instance, 'name' là tham số
  }
  ```
* **Công dụng 2: Gọi Constructor khác trong cùng Class (`this(...)`)**
  Dùng để tái sử dụng logic khởi tạo giữa các Constructor.
  ```java
  public Student() { this("Unknown"); }
  public Student(String name) { this.name = name; }
  ```

---

## 2. Từ khóa `super`
`super` là một tham chiếu đến **đối tượng của lớp cha** (parent instance).

* **Công dụng 1: Truy cập thành viên của lớp cha**
  Dùng khi con override phương thức của cha nhưng vẫn muốn gọi lại logic của cha, hoặc khi biến con bị shadowing biến cha.
  ```java
  public void talk() {
      super.talk(); // Gọi phương thức của cha
      System.out.println("Child talk");
  }
  ```
* **Công dụng 2: Gọi Constructor của lớp cha (`super(...)`)**
  Dùng để gọi constructor của cha trong constructor của con.

---

## 3. Quy tắc "Sống còn" (Constructor Rules)
Đây là phần trọng tâm thi OCA/OCP, em cần học thuộc:

1. **Phải là lệnh đầu tiên:** `this()` hoặc `super()` bắt buộc phải là dòng mã đầu tiên trong thân Constructor.
2. **Không được gọi cả hai:** Em **không bao giờ** được gọi cả `this()` và `super()` trong cùng một Constructor.
3. **Mặc định:** Nếu em không viết `this()` hay `super()`, trình biên dịch sẽ tự động thêm `super()` vào dòng đầu tiên (gọi constructor không tham số của cha).
4. **Không dùng trong Static:** `this` và `super` không bao giờ được dùng trong phương thức `static` hoặc `static block` vì lúc đó chưa có đối tượng cụ thể nào được tạo ra.

---

## 4. Bảng so sánh nhanh

| Đặc điểm | `this` | `super` |
| :--- | :--- | :--- |
| **Mục đích** | Tham chiếu tới chính đối tượng hiện tại. | Tham chiếu tới đối tượng cha. |
| **Constructor** | `this(...)` - Gọi constructor khác cùng class. | `super(...)` - Gọi constructor lớp cha. |
| **Truy cập** | `this.variable`, `this.method()` | `super.variable`, `super.method()` |

---

## 5. Bẫy OCA/OCP cần lưu ý

### ⚠️ Bẫy 1: Quên Constructor không tham số ở lớp cha
Nếu lớp cha có định nghĩa Constructor có tham số, trình biên dịch sẽ **không** tự tạo Constructor không tham số mặc định. Lúc này, nếu lớp con gọi `super()` (mặc định) sẽ bị lỗi biên dịch.
* **Cách sửa:** Lớp con buộc phải gọi tường minh `super(tham_số_của_cha)`.

### ⚠️ Bẫy 2: Lầm tưởng `this` và `super` là đối tượng
`this` và `super` là **từ khóa/tham chiếu**, không phải là đối tượng. Tuy nhiên, `this` trỏ tới đối tượng hiện tại, và `super` trỏ tới phần của cha trong đối tượng hiện tại đó.

---
*Chúc em ôn tập tốt! Nếu cần bài tập thực hành về phần Constructor này, hãy báo anh ngay nhé!*
