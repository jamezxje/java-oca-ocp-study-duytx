# Generics, Wildcards (PECS) và Type Erasure Trong Java

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Generics, Quy tắc PECS và Cơ chế Type Erasure** dành cho lập trình viên Java 2 năm kinh nghiệm chuẩn bị phỏng vấn và thi chứng chỉ OCP.

---

## Phần 1: Bản Chất Của Generics & Cơ Chế Type Erasure

### 1. Tại sao Java cần Generics (Java 5+)?
* **Type Safety (An toàn kiểu dữ liệu):** Phát hiện lỗi sai kiểu ngay tại thời điểm biên dịch (Compile-time) thay vì bị ném ra `ClassCastException` tại thời điểm chạy (Runtime).
* **Loại bỏ ép kiểu thủ công (Eliminate Casting):** Không cần phải viết `(String) list.get(0)`.

---

### 2. Cơ Chế Type Erasure (Xóa Bỏ Kiểu) - Trọng Tâm OCP & Phỏng Vấn

Một câu hỏi phỏng vấn kinh điển: **"JVM xử lý Generics như thế nào tại Runtime?"**

#### Bản chất:
* Java giới thiệu Generics ở Java 5 nhưng phải đảm bảo tính **Tương thích ngược (Backward Compatibility)** với các phiên bản Java 1.x cũ.
* Do đó, Trình biên dịch (Compiler) sẽ **xóa bỏ toàn bộ thông tin kiểu Generics** sau khi kiểm tra xong tại thời điểm biên dịch.
* Tại Runtime, JVM **KHÔNG hề biết** `List<String>` hay `List<Integer>` là gì, cả hai đều trở thành raw type **`List` (hoặc `Object`)**.

#### Quy tắc biến đổi của Compiler khi Erasure:
1. Thay thế tất cả Type Parameter bằng `Object` (nếu là Unbounded `<T>`) hoặc bằng Bounded Type (nếu là `<T extends Number>`).
2. Tự động chèn các lệnh ép kiểu (Casting) ở những nơi lấy dữ liệu ra.
3. Tạo các phương thức Bridge (Bridge Methods) để duy trì tính đa hình.

```java
// Mã nguồn bạn viết:
public class Box<T> {
    private T data;
    public T getData() { return data; }
}

// Mã nguồn sau khi Compiler thực hiện Type Erasure (Bytecode):
public class Box {
    private Object data;
    public Object getData() { return data; }
}
```

---

### 3. Hạn Chế Của Generics Do Type Erasure

Do thông tin kiểu bị xóa tại Runtime, Generics bị các hạn chế sau:
1. **Không dùng được Kiểu nguyên thủy (Primitive Types):** Không thể khai báo `List<int>`, phải dùng Wrapper Class `List<Integer>`.
2. **Không thể khởi tạo đối tượng Generics:** `new T()` là **LỖI BIÊN DỊCH** (vì JVM không biết `T` là class nào để cấp phát).
3. **Không thể tạo mảng Generics:** `new T[10]` hoặc `new List<String>[10]` là **LỖI BIÊN DỊCH**.
4. **Không thể dùng Generics với từ khóa `static`:** Biến static dùng chung cho toàn class, không thể khai báo `private static T data;`.
5. **Không thể dùng trong Exception Handling:** Class ném ngoại lệ không được kế thừa `Throwable` dạng Generics (`class MyException<T> extends Exception` là LỖI).

---

## Phần 2: Wildcards & Quy Tắc PECS (Producer Extends, Consumer Super)

Ký tự đại diện `?` (Wildcard) giúp tăng tính linh hoạt cho Generics trong kế thừa.

### 1. Phân biệt 3 loại Wildcards

| Cú pháp | Tên gọi | Bản chất |
| :--- | :--- | :--- |
| **`List<?>`** | Unbounded Wildcard | Danh sách chứa bất kỳ kiểu dữ liệu nào (Đọc được dưới dạng `Object`, không thêm mới được trừ `null`). |
| **`List<? extends Number>`** | Upper Bounded Wildcard | Danh sách chứa `Number` hoặc các lớp con của `Number` (`Integer`, `Double`...). |
| **`List<? super Integer>`** | Lower Bounded Wildcard | Danh sách chứa `Integer` hoặc các lớp cha của `Integer` (`Number`, `Object`). |

---

### 2. Quy Tắc PECS (Producer Extends, Consumer Super) - Nguyên Lý Thiết Kế API

Một câu hỏi phỏng vấn phân loại Senior: **"Khi nào nên dùng `? extends T` và khi nào nên dùng `? super T`?"**

> **PECS:** **P**roducer **E**xtends, **C**onsumer **S**uper.

* **Producer Extends (`? extends T`):**
  * Dùng khi bộ sưu tập đóng vai trò **Cung cấp dữ liệu (Producer)** cho ứng dụng của bạn (bạn chỉ ĐỌC dữ liệu từ bộ sưu tập ra).
  * *Ràng buộc:* Bạn **KHÔNG THỂ THÊM** bất kỳ phần tử nào vào danh sách `? extends T` (trừ `null`), vì Compiler không thể đảm bảo kiểu cụ thể của mảng.
  ```java
  // Chỉ ĐỌC dữ liệu ra (Producer)
  public double sumOfList(List<? extends Number> list) {
      double sum = 0.0;
      for (Number n : list) {
          sum += n.doubleValue(); // Hợp lệ!
      }
      // list.add(10); // ❌ LỖI BIÊN DỊCH!
      return sum;
  }
  ```

* **Consumer Super (`? super T`):**
  * Dùng khi bộ sưu tập đóng vai trò **Tiêu thụ dữ liệu (Consumer)** (bạn chỉ GHI/THÊM dữ liệu vào bộ sưu tập).
  * *Ràng buộc:* Bạn có thể an toàn **THÊM** các đối tượng kiểu `T` (hoặc lớp con của `T`) vào danh sách.
  ```java
  // Chỉ GHI dữ liệu vào (Consumer)
  public void addIntegers(List<? super Integer> list) {
      list.add(1);  // Hợp lệ!
      list.add(2);  // Hợp lệ!
      // Number n = list.get(0); // ❌ LỖI! Dữ liệu đọc ra chỉ đảm bảo là kiểu Object.
  }
  ```

---

## Phần 3: Comparable vs Comparator

Dùng để sắp xếp các phần tử Generics trong `Collections.sort()` hay `TreeMap`/`TreeSet`.

| Tiêu chí | `Comparable<T>` | `Comparator<T>` |
| :--- | :--- | :--- |
| **Vị trí khai báo** | Khai báo trực tiếp bên trong Class đối tượng. | Khai báo ở một Class riêng hoặc Lambda Expression độc lập. |
| **Phương thức trừu tượng** | `int compareTo(T o)` | `int compare(T o1, T o2)` |
| **Số lượng tiêu chí** | Chỉ định nghĩa được **1 tiêu chí sắp xếp tự nhiên** duy nhất. | Định nghĩa được **nhiều tiêu chí tùy chỉnh** (theo tên, theo giá, theo tuổi...). |
| **Sử dụng Lambda** | Không hỗ trợ trực tiếp. | Hỗ trợ Lambda & Method Reference rất mạnh (`Comparator.comparing(...)`). |

---

## 📌 Summary Sheet Chặng 4

1. **Type Erasure:** Xóa toàn bộ Generics tại Runtime $\rightarrow$ `List<String>` biến thành raw type `List`.
2. **Hạn chế Generics:** Không `new T()`, không mảng Generics, không Primitive types, không `static T`.
3. **PECS Rule:** 
   * **Producer Extends (`? extends T`)**: Chỉ ĐỌC dữ liệu, KHÔNG được `add()`.
   * **Consumer Super (`? super T`)**: Chỉ GHI (`add()`) dữ liệu vào.
4. **Comparable vs Comparator:** `Comparable` là thứ tự tự nhiên (gắn trong class); `Comparator` là thứ tự tùy chỉnh (dùng ngoài/Lambda).

---
*Tài liệu này tổng hợp toàn bộ trọng tâm Generics & Wildcards!*
