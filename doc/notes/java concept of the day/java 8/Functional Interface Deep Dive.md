# Functional Interface Trong Java 8+: Bản Chất, Lý Do Và Cơ Chế Vận Hành

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Functional Interface (Giao diện chức năng)**, lý do ra đời, cách thức hoạt động bên dưới JVM (`invokedynamic`) và 4 interface cốt lõi.

---

## 1. Functional Interface Là Gì?

**Functional Interface (Giao diện chức năng)** trong Java 8+ là một Interface **chỉ chứa duy nhất MỘT phương thức trừu tượng (Single Abstract Method - SAM)**.

```java
@FunctionalInterface
public interface Calculator {
    int calculate(int a, int b); // Duy nhất 1 abstract method (SAM)
}
```

### 💡 Các quy tắc quan trọng về mặt cú pháp:
1. **Annotation `@FunctionalInterface`:** Dùng để báo cho trình biên dịch (Compiler) kiểm tra tính hợp lệ. Nếu Interface có 0 hoặc $\ge 2$ abstract method, Compiler sẽ báo lỗi ngay lập tức. (Annotation này không bắt buộc nhưng khuyên dùng).
2. **Vẫn được chứa Default & Static methods:** Functional Interface **được phép chứa nhiều `default methods` và `static methods`** (vì các phương thức này đã có phần thân), miễn là chỉ có **đúng 1 phương thức trừu tượng**.
3. **Phương thức của `java.lang.Object`:** Việc khai báo lại các phương thức `public` của `Object` (`equals()`, `toString()`, `hashCode()`) trong Interface **KHÔNG tính** vào phương thức trừu tượng SAM.

```java
@FunctionalInterface
public interface MyFunctionalInterface {
    void execute(); //  Phương thức trừu tượng duy nhất (SAM)

    default void log() { System.out.println("Logging..."); } // Hợp lệ
    static void info() { System.out.println("Info..."); }     // Hợp lệ
    boolean equals(Object obj);                              // Hợp lệ (kế thừa Object)
}
```

---

## 2. Tại Sao Lại Cần Functional Interface?

Trước Java 8 (Java 7 trở về trước), Java là một ngôn ngữ thuần Hướng đối tượng (OOP). Muốn truyền một **"hành vi" (Behavior/Function)** vào một phương thức khác, lập trình viên bắt buộc phải tạo một **Anonymous Inner Class (Lớp nội danh)**.

### So sánh code trước và sau Java 8:

#### Cách viết cũ (Java 7 - Rườm rà, tốn code):
```java
// Muốn chạy 1 Thread, phải tạo Anonymous Inner Class của Runnable
Thread t = new Thread(new Runnable() {
    @Override
    public void run() {
        System.out.println("Thread đang chạy...");
    }
});
```

#### Cách viết mới (Java 8 - Ngắn gọn với Lambda & Functional Interface):
```java
// Runnable là một @FunctionalInterface -> Dùng Lambda ngắn gọn 1 dòng!
Thread t = new Thread(() -> System.out.println("Thread đang chạy..."));
```

---

### 🌟 3 Lý Do Cốt Lõi Java Cần Functional Interface:

1. **Làm "Target Type" (Kiểu mục tiêu) Cho Lambda Expression:**
   Java là ngôn ngữ **Strongly Typed (Kiểu dữ liệu chặt chẽ)**. Biểu thức Lambda `() -> System.out.println()` không thể đứng độc lập mà phải có một kiểu dữ liệu đại diện. Functional Interface chính là **khung kiểu dữ liệu** để chứa Lambda Expression.
2. **Cho Phép Truyền Hàm Như Tham Số (Higher-Order Functions):**
   Giúp lập trình viên truyền trực tiếp thuật toán/hành vi làm tham số cho hàm khác (ví dụ: truyền thuật toán lọc vào hàm `.filter()`).
3. **Làm Nền Tảng Cho Stream API:**
   Toàn bộ Stream API (`map`, `filter`, `reduce`, `forEach`) đều dựa hoàn toàn vào các Functional Interface.

---

## 3. Cơ Chế Vận Hành Bên Trong JVM (Under The Hood)

Một câu hỏi phỏng vấn phân loại Senior: **"Bên dưới JVM, Lambda Expression và Functional Interface hoạt động thế nào? Có tạo ra file `.class` mới như Anonymous Inner Class không?"**

### A. Sự khác biệt ở cấp độ Bytecode:

| Tiêu chí | Anonymous Inner Class (Java 7) | Lambda & Functional Interface (Java 8+) |
| :--- | :--- | :--- |
| **Tạo file `.class`** | **Có.** Mỗi Anonymous Class tạo ra 1 file `.class` riêng trên ổ đĩa (ví dụ `Main$1.class`). | **KHÔNG.** Không sinh ra file `.class` mới trên ổ đĩa. |
| **Lệnh Bytecode** | Lệnh `new` + `invokespecial` (tốn chi phí nạp class vào RAM). | Lệnh bytecode đặc biệt **`invokedynamic`**. |
| **Chi phí bộ nhớ** | Tốn bộ nhớ Metaspace để lưu thông tin class con. | Sinh ra CallSite trực tiếp **In-Memory** lúc Runtime, cực kỳ tiết kiệm bộ nhớ. |

### B. Thuật toán `invokedynamic` & `LambdaMetafactory`:
1. Khi trình biên dịch gặp một Lambda Expression, nó **không** tạo class con. Nó tạo một lệnh bytecode `invokedynamic`.
2. Khi JVM chạy đến câu lệnh này lần đầu tiên, nó gọi tới **`java.lang.invoke.LambdaMetafactory`**.
3. `LambdaMetafactory` sẽ tạo ra một instance triển khai Functional Interface đó **ngay trong bộ nhớ RAM** tại thời điểm Runtime.

---

## 4. Bộ 4 Functional Interfaces Cốt Lõi Trong Java

Java 8 cung cấp sẵn 4 Functional Interfaces cơ bản trong gói `java.util.function`:

```
1. Supplier<T>   : () -> T           (Cung cấp dữ liệu)
2. Consumer<T>   : (T) -> void       (Tiêu thụ dữ liệu)
3. Predicate<T>  : (T) -> boolean    (Kiểm tra điều kiện)
4. Function<T,R> : (T) -> R          (Biến đổi dữ liệu)
```

### Ví dụ minh họa thực tế:

```java
import java.util.function.*;

public class Main {
    public static void main(String[] args) {
        // 1. Supplier: Không tham số, trả về chuỗi "Hello World"
        Supplier<String> supplier = () -> "Hello World";
        System.out.println(supplier.get()); // Out: Hello World

        // 2. Consumer: Nhận 1 chuỗi, in ra màn hình (không trả về)
        Consumer<String> consumer = (text) -> System.out.println("Processing: " + text);
        consumer.accept("Java 8"); // Out: Processing: Java 8

        // 3. Predicate: Kiểm tra số chẵn
        Predicate<Integer> isEven = (num) -> num % 2 == 0;
        System.out.println(isEven.test(4)); // Out: true

        // 4. Function: Biến đổi chuỗi sang độ dài (String -> Integer)
        Function<String, Integer> getLength = (str) -> str.length();
        System.out.println(getLength.apply("Antigravity")); // Out: 11
    }
}
```

---

## 📌 Summary Sheet Chặng Functional Interface

1. **Khái niệm:** Interface chỉ chứa **đúng 1 phương thức trừu tượng (SAM)**.
2. **Lý do cần:** Làm khung kiểu dữ liệu cho **Lambda Expression**, giúp truyền hành vi làm tham số và làm nền tảng cho Stream API.
3. **Cơ chế JVM:** Dùng lệnh bytecode **`invokedynamic`** + `LambdaMetafactory`, sinh instance **In-Memory** lúc Runtime mà **không sinh ra file `.class` rác**.

---
*Tài liệu này hệ thống toàn bộ cơ chế Functional Interface!*
