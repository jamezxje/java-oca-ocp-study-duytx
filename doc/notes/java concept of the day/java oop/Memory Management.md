# Memory Management (Quản lý bộ nhớ) trong Java

Chào em! Là Java mentor của em, anh đã tổng hợp kiến thức về **Memory Management** trong Java – một chủ đề "xương sống" để hiểu cách Java vận hành thực sự dưới lớp vỏ bọc JVM. Đây là nội dung quan trọng trong OCP, đặc biệt khi cần tối ưu hóa hiệu suất ứng dụng.

---

## 1. Mô hình bộ nhớ JVM (JVM Memory Model)

JVM chia bộ nhớ thành các phần chính, mỗi phần có vai trò và vòng đời khác nhau:

### A. Stack Memory (Bộ nhớ ngăn xếp)
* **Đặc điểm:** Hoạt động theo cơ chế **LIFO** (Last In, First Out).
* **Nội dung:** Chứa **biến cục bộ (local variables)**, biến tham chiếu (reference variables) trỏ tới đối tượng trên Heap, và các frame của phương thức khi được gọi.
* **Tốc độ:** Rất nhanh.
* **Vòng đời:** Gắn liền với vòng đời của phương thức. Khi phương thức kết thúc, dữ liệu trong Stack frame tương ứng sẽ tự động bị xóa.
* **Lỗi:** Ném ra `StackOverflowError` nếu gọi phương thức đệ quy quá sâu.

### B. Heap Memory (Bộ nhớ Heap)
* **Đặc điểm:** Là vùng nhớ lớn dùng chung cho toàn bộ ứng dụng.
* **Nội dung:** Chứa tất cả các **đối tượng (Objects)** và **mảng (Arrays)**. Bất kể em tạo bằng `new` hay literal (String), cuối cùng đối tượng đều nằm trên Heap.
* **Tốc độ:** Chậm hơn Stack.
* **Vòng đời:** Do Garbage Collector (GC) quản lý.
* **Lỗi:** Ném ra `OutOfMemoryError` nếu bộ nhớ Heap không đủ cấp phát.

### C. Metaspace (Từ Java 8+)
* **Đặc điểm:** Thay thế cho PermGen cũ. Nó nằm trong bộ nhớ gốc (Native Memory) của hệ điều hành, không phải bộ nhớ của JVM Heap.
* **Nội dung:** Chứa **metadata của class** (thông tin cấu trúc lớp, phương thức, hằng số tĩnh, v.v.).

---

## 2. Garbage Collection (Thu gom rác)

Java tự động thu hồi bộ nhớ không còn sử dụng thông qua tiến trình **Garbage Collector (GC)**.

### Đối tượng khi nào đủ điều kiện (eligible) để GC thu gom?
Đây là trọng tâm của các câu hỏi bẫy OCA/OCP. Một đối tượng được coi là đủ điều kiện khi:
1. **Không còn tham chiếu (Reference) nào trỏ tới nó:** Biến tham chiếu đã ra khỏi scope (ví dụ: phương thức kết thúc) hoặc đã bị gán bằng `null`.
2. **Tham chiếu vòng (Island of Isolation):** Hai đối tượng trỏ vào nhau nhưng không có tham chiếu nào từ bên ngoài trỏ tới nhóm đó.

```java
class A { A ref; }
A a1 = new A();
A a2 = new A();
a1.ref = a2;
a2.ref = a1;
a1 = null; a2 = null; // Cả hai đối tượng này giờ đủ điều kiện GC
```

### Lưu ý quan trọng về `System.gc()`:
* Việc gọi `System.gc()` chỉ là một **gợi ý (hint)** cho JVM để chạy GC, không phải là lệnh bắt buộc. JVM hoàn toàn có quyền phớt lờ lệnh này. **Không bao giờ tin tưởng vào `System.gc()` trong code production!**

---

## 3. OCA/OCP "Cạm bẫy" cần lưu ý

### ⚠️ Bẫy 1: Sự khác biệt giữa StackOverflowError và OutOfMemoryError
* `StackOverflowError`: Xảy ra do Stack đầy (thường do đệ quy quá sâu hoặc quá nhiều lời gọi phương thức lồng nhau).
* `OutOfMemoryError`: Xảy ra do Heap đầy hoặc Metaspace đầy. GC đã chạy hết công suất nhưng vẫn không đủ chỗ để cấp phát đối tượng mới.

### ⚠️ Bẫy 2: Đối tượng không bao giờ bị GC thu gom
* Các đối tượng đang nằm trong `static` reference.
* Các đối tượng đang nằm trong String Pool (nếu vẫn còn reference trỏ tới).
* Các đối tượng thuộc các thread đang chạy.

### ⚠️ Bẫy 3: Phương thức `finalize()`
* Phương thức `Object.finalize()` (đã bị deprecated từ Java 9) **không đảm bảo** sẽ được gọi khi đối tượng bị GC thu gom.
* Đừng bao giờ dựa dẫm vào `finalize()` để giải phóng tài nguyên. Hãy sử dụng `try-with-resources` hoặc block `finally` cho mục đích đó.

---
*Hy vọng tài liệu này giúp em có cái nhìn tổng quan về cách Java quản lý bộ nhớ. Phần này thường đi kèm với việc hiểu sâu về String Pool và cơ chế hoạt động của GC. Nếu cần anh lấy ví dụ cụ thể về việc "khi nào đối tượng trở thành rác", hãy nhắn anh nhé! Chúc em học tốt!*
