# Instance Initialization Block (IIB) trong Java

Chào em! Là Java mentor của em, anh đã biên soạn lại chi tiết tài liệu học tập về **Instance Initialization Block (IIB)** trực tiếp vào sổ tay học tập của em. Đây là một chủ đề cực kỳ quan trọng và xuất hiện rất nhiều trong các câu hỏi bẫy của kỳ thi OCA/OCP.

Dưới đây là tất cả những điều em cần nắm vững về IIB:

---

## 1. Khái niệm (Concept)
**Instance Initialization Block (IIB)** (Khối khởi tạo thực thể) là một khối mã (block of code) nằm trực tiếp bên trong một class, không nằm trong bất kỳ phương thức hay constructor nào, và **không** có từ khóa `static`.

* **Cú pháp:** Đơn giản là cặp dấu ngoặc nhọn `{}` chứa mã nguồn.
```java
class Student {
    // Đây là một Instance Initialization Block (IIB)
    {
        System.out.println("IIB đang chạy...");
    }
}
```

---

## 2. Khi nào IIB chạy? (Execution Timing)
* IIB được thực thi **mỗi khi một đối tượng mới được tạo** (bằng từ khóa `new`).
* Về mặt bản chất dưới JVM: Mã nguồn trong IIB sẽ được trình biên dịch **sao chép (copy) vào đầu mỗi constructor** của lớp đó (ngay sau lời gọi `super()`).
* Do đó, thứ tự chạy là: **Lớp cha hoàn tất khởi tạo (`super()`) -> IIB chạy -> Phần thân Constructor hiện tại chạy.**

---

## 3. Tại sao cần sử dụng IIB? (Use Cases)
Có 3 lý do chính để sử dụng IIB thay vì viết code trực tiếp trong constructor:

1. **Tái sử dụng mã nguồn (Code Reuse):**
   Nếu một class có nhiều constructor nạp chồng (overloaded constructors) và tất cả chúng đều yêu cầu một đoạn mã khởi tạo chung, em có thể đưa đoạn mã đó vào IIB. IIB sẽ tự động chạy cho mọi constructor mà không cần em phải viết lại code hay lặp lại lời gọi `this()`.
   
2. **Khởi tạo logic phức tạp cho biến instance:**
   Khi em cần gán giá trị cho một thuộc tính đối tượng nhưng cần dùng các câu lệnh rẽ nhánh (`if-else`), vòng lặp (`for`/`while`), hoặc khối `try-catch` để xử lý ngoại lệ mà không muốn làm constructor bị rối rắm.

3. **Khởi tạo cho Anonymous Inner Class (Lớp nội danh):**
   Anonymous Inner Class không có tên class, do đó **không thể định nghĩa constructor**. Cách duy nhất để chạy mã khởi tạo cho chúng là sử dụng IIB.
   ```java
   Thread t = new Thread() {
       {
           setName("My-Anonymous-Thread");
       }
       @Override
       public void run() {
           System.out.println(getName() + " đang chạy...");
       }
   };
   ```

---

## 4. Thứ tự khởi tạo toàn diện (Initialization Order) - Trọng tâm OCA/OCP!
Đây là phần **bắt buộc** phải thuộc lòng để đi thi. Khi một đối tượng của lớp con (`Subclass`) được khởi tạo, thứ tự thực thi của JVM như sau:

1. **Static Initializer Blocks / Static Variables của lớp CHA (Superclass):** Khởi tạo theo thứ tự xuất hiện từ trên xuống dưới. (Chỉ chạy 1 lần duy nhất khi load class).
2. **Static Initializer Blocks / Static Variables của lớp CON (Subclass):** Khởi tạo theo thứ tự xuất hiện từ trên xuống dưới. (Chỉ chạy 1 lần duy nhất).
3. **Instance Variables & IIB của lớp CHA (Superclass):** Chạy theo thứ tự xuất hiện từ trên xuống dưới trong file.
4. **Constructor của lớp CHA (Superclass).**
5. **Instance Variables & IIB của lớp CON (Subclass):** Chạy theo thứ tự xuất hiện từ trên xuống dưới trong file.
6. **Constructor của lớp CON (Subclass).**

### Ví dụ thực tế minh họa thứ tự:
```java
class Parent {
    static { System.out.println("1. Static Parent"); }
    { System.out.println("3. IIB Parent"); }
    public Parent() { System.out.println("4. Constructor Parent"); }
}

class Child extends Parent {
    static { System.out.println("2. Static Child"); }
    { System.out.println("5. IIB Child"); }
    public Child() { System.out.println("6. Constructor Child"); }

    public static void main(String[] args) {
        System.out.println("--- Bắt đầu tạo đối tượng Child ---");
        new Child();
    }
}
```
**Kết quả in ra màn hình:**
```text
1. Static Parent
2. Static Child
--- Bắt đầu tạo đối tượng Child ---
3. IIB Parent
4. Constructor Parent
5. IIB Child
6. Constructor Child
```
*Lưu ý:* Nếu tiếp tục gọi `new Child()` lần thứ hai, phần static (1 và 2) sẽ **không** chạy lại nữa, mà chỉ chạy từ bước 3 đến 6.

---

## 5. Các quy tắc quan trọng và Bẫy thường gặp (Important Rules & Pitfalls)

### Quy tắc 1: Có thể có nhiều IIB trong một Class
Một lớp có thể chứa bao nhiêu IIB tùy ý. Chúng sẽ thực thi theo đúng thứ tự xuất hiện từ trên xuống dưới trong mã nguồn.

### Quy tắc 2: Bẫy Forward Reference (Tham chiếu ngược)
Một IIB **không thể đọc trực tiếp** giá trị của một biến instance nếu biến đó được khai báo ở dòng phía sau nó (Compiler Error: *Illegal forward reference*). Tuy nhiên, em có thể **gán** giá trị cho nó hoặc truy cập gián tiếp qua `this`.
```java
class Demo {
    {
        // System.out.println(x); // LỖI BIÊN DỊCH: Illegal forward reference!
        System.out.println(this.x); // HỢP LỆ (nhưng x lúc này vẫn bằng 0/mặc định)
        x = 10; // HỢP LỆ (phép gán trực tiếp)
    }
    int x = 5; // Dòng khai báo nằm sau IIB
}
```
*Hỏi nhỏ:* Sau khi `new Demo()` chạy xong, giá trị của `x` là bao nhiêu?
*Trả lời:* Là `5`. Vì IIB chạy trước (gán `x = 10`), sau đó dòng `int x = 5;` chạy tiếp theo thứ tự từ trên xuống dưới và ghi đè giá trị của `x` thành `5`.

### Quy tắc 3: Xử lý ngoại lệ (Exception Handling) trong IIB
* Nếu IIB ném ra một **checked exception** (ví dụ: `IOException`, `SQLException`), thì **tất cả** các constructor của class đó bắt buộc phải khai báo ngoại lệ đó trong mệnh đề `throws` của mình.
* Nếu là **unchecked exception** (ví dụ: `NullPointerException`, `ArithmeticException`), constructor không cần khai báo gì cả.
```java
import java.io.IOException;

class FileLoader {
    {
        if (true) throw new IOException("Lỗi đọc file"); // Checked Exception
    }

    // Bắt buộc tất cả Constructor phải khai báo throws IOException
    public FileLoader() throws IOException {
    }
    
    public FileLoader(String path) throws IOException {
    }
}
```

---

## 6. So sánh giữa Static Block, IIB và Constructor

| Đặc điểm | Static Initialization Block | Instance Initialization Block (IIB) | Constructor |
| :--- | :--- | :--- | :--- |
| **Từ khóa** | Cần từ khóa `static` | Không cần từ khóa | Tên trùng với Class, không có kiểu trả về |
| **Số lần chạy** | **Chỉ 1 lần duy nhất** khi class được nạp vào bộ nhớ JVM | **Mỗi khi** đối tượng mới được tạo bằng `new` | **Mỗi khi** đối tượng mới được tạo bằng `new` |
| **Thứ tự chạy** | Chạy trước tất cả IIB và Constructor | Chạy sau `super()` nhưng trước thân Constructor | Chạy cuối cùng |
| **Truy cập** | Chỉ truy cập được các thành viên `static` | Truy cập được cả `static` và non-static | Truy cập được cả `static` và non-static |

---

Hãy đọc kỹ và lưu giữ những kiến thức này nhé. Nếu có chỗ nào chưa hiểu rõ hoặc muốn thử sức với các bài tập bẫy OCA/OCP về phần này, cứ nói với anh! Chúc em học tốt!
