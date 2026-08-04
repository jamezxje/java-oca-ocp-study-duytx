# Quản Lý Bộ Nhớ Nâng Cao, Memory Leaks & Garbage Collection (GC)

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Mô hình bộ nhớ JVM, Các nguyên nhân Memory Leak thực tế và Thuật toán Garbage Collection** dành cho lập trình viên Java 2 năm kinh nghiệm chuẩn bị phỏng vấn và thi chứng chỉ OCP.

---

## Phần 1: Mô Hình Bộ Nhớ JVM Chi Tiết (JVM Memory Model)

Bộ nhớ JVM chia thành 5 vùng chính:

```
┌─────────────────────────────────────────────────────────────┐
│                          JVM HEAP                           │
│  ┌───────────────────────────────┐ ┌─────────────────────┐  │
│  │   Young Generation            │ │   Old Generation    │  │
│  │  ┌──────────┬───────┬───────┐ │ │   (Tenured Space)   │  │
│  │  │   Eden   │ S0    │ S1    │ │ │                     │  │
│  │  └──────────┴───────┴───────┘ │ │                     │  │
│  └───────────────────────────────┘ └─────────────────────┘  │
└─────────────────────────────────────────────────────────────┘
┌─────────────────────────────────────────────────────────────┐
│                Metaspace (Native Memory - OS)                │
└─────────────────────────────────────────────────────────────┘
```

### 1. Heap Memory (Bộ nhớ Heap)
Là nơi chứa tất cả **Objects** và **Arrays** trong ứng dụng Java. Được chia làm 2 vùng chính:
* **Young Generation:** Nơi đối tượng vừa được tạo ra bằng `new`.
  * **Eden Space:** Đối tượng mới sinh ra nằm ở đây.
  * **Survivor Spaces (S0 và S1):** Chứa các đối tượng sống sót qua các đợt **Minor GC**.
* **Old Generation (Tenured):** Chứa các đối tượng sống lâu (sống sót qua nhiều lần Minor GC, đạt đến ngưỡng `TenuringThreshold` mặc định 15) hoặc các đối tượng quá lớn.

---

### 2. Metaspace (Từ Java 8+)
* Thay thế cho **PermGen** cũ (vốn nằm trong Heap và bị giới hạn kích thước).
* **Vị trí:** Nằm hoàn toàn trong bộ nhớ gốc (**Native Memory**) của Hệ điều hành.
* **Chức năng:** Lưu trữ Metadata của Class (Cấu trúc lớp, Bytecode, Phương thức, Static Variables).

---

### 3. Stack Memory (Bộ nhớ Stack)
* Mỗi Thread khi tạo ra có một Stack Memory riêng.
* Chứa các **Stack Frame** tương ứng với từng lời gọi phương thức.
* Lưu trữ biến cục bộ (Local Variables), biến tham chiếu (Reference Variables) và các phép tính toán trung gian.
* Khi phương thức kết thúc, Stack Frame tương ứng bị xóa ngay lập tức ($O(1)$).

---

## Phần 2: Cơ Chế Thu Gom Rác (Garbage Collection - GC)

### 1. Phân biệt Minor GC vs Major GC vs Full GC

| Loại GC | Vùng bộ nhớ xử lý | Tốc độ | Tác động ứng dụng |
| :--- | :--- | :--- | :--- |
| **Minor GC** | Chỉ dọn dẹp vùng **Young Generation** (Eden + Survivor). | Cực kỳ nhanh. | Tạm dừng ngắn (Stop-The-World ngắn). |
| **Major GC** | Chỉ dọn dẹp vùng **Old Generation**. | Chậm hơn Minor GC 10x. | Tạm dừng dài hơn. |
| **Full GC** | Dọn dẹp **toàn bộ bộ nhớ** (Heap + Metaspace). | Cực kỳ chậm. | Gây **Stop-The-World (STW)** kéo dài, nghẽn ứng dụng. |

---

### 2. Tổng Quan Các Bộ Thu Gom Rác Hiện Đại (GC Collectors)

Một câu hỏi phỏng vấn thực tế: **"Dự án của bạn đang dùng GC Collector nào và khi nào nên chuyển sang ZGC/G1GC?"**

1. **Serial GC:** Đơn luồng, thích hợp cho ứng dụng nhỏ, thiết bị IoT/Embedded.
2. **Parallel GC (Throughput Collector):** Đa luồng, tối ưu cho **Tổng năng suất (Throughput)**, chấp nhận STW ngắn. (Mặc định ở Java 8).
3. **G1GC (Garbage-First GC):** Chia Heap thành nhiều Region nhỏ. Tối ưu cho Heap dung lượng lớn ($> 4GB$) với mục tiêu kiểm soát thời gian tạm dừng STW ngắn. (Mặc định từ Java 9+).
4. **ZGC (Z Garbage Collector):** Bộ thu gom latency cực thấp (Sub-millisecond STW pause time) xử lý được Heap từ vài Gigabyte đến hàng Terabyte!

---

## Phần 3: Memory Leaks Trong Java Thực Tế (CỰC KỲ QUAN TRỌNG)

Dù Java có GC tự động, ứng dụng Java vẫn bị lỗi **Memory Leak** (Rò rỉ bộ nhớ) khi một đối tượng **không còn được ứng dụng sử dụng nữa nhưng vẫn còn tham chiếu (Reference) trỏ tới nó**, khiến GC không bao giờ thu gom được.

---

### 5 Nguyên Nhân Gây Memory Leak Phổ Biến Trong Dự Án:

1. **Biến Tĩnh (`static`) Giữ Tham Chiếu Đến Đối Tượng Thường:**
   * Biến `static` sống suốt vòng đời của ứng dụng. Nếu bạn cho một `List` hay `Map` vào một biến `static` mà không xóa bớt phần tử cũ đi, nó sẽ phình to mãi mãi.

2. **Khóa `HashMap` Không Override `equals()` & `hashCode()`:**
   * Khi thêm phần tử vào `HashMap`, rồi tìm cách `remove()`, nếu không override đúng `equals/hashCode`, đối tượng đó bị kẹt vĩnh viễn trong Map mà không lấy ra hay xóa đi được.

3. **Quên Đóng Tài Nguyên (Unclosed Resources):**
   * Mở file (`InputStream`), kết nối DB (`Connection`, `ResultSet`), Socket mà quên đóng trong block `finally` hoặc không dùng `try-with-resources`.

4. **Outer Class bị giữ bởi Non-Static Inner Class:**
   * Lớp nội phi tĩnh (Non-static Inner Class) luôn giữ một tham chiếu ngầm (`Outer.this`) tới đối tượng lớp ngoài. Nếu đối tượng nội sống lâu, nó sẽ kéo theo đối tượng lớp ngoài không thể bị GC thu gom.

5. **Lắng Nghe Sự Kiện (Unregistered Listeners / Observers):**
   * Đăng ký listener vào Event Bus hay Callback mà quên hủy đăng ký (`unregister()`) khi Component bị hủy.

---

## Phần 4: Các Kiểu Reference Trong Java (Strong, Soft, Weak, Phantom)

Java cung cấp 4 loại tham chiếu trong gói `java.lang.ref`:

| Kiểu Reference | Khi nào GC thu gom? | Ứng dụng thực tế |
| :--- | :--- | :--- |
| **Strong Reference** (`Object obj = new Object()`) | Không bao giờ bị GC thu gom ngoại trừ khi bị gán `null` hoặc ra khỏi scope. | Khai báo biến thông thường. |
| **Soft Reference** (`SoftReference<T>`) | GC chỉ thu gom khi bộ nhớ Heap **sắp cạn kiệt (Sắp bị OutOfMemoryError)**. | Làm bộ nhớ đệm Cache nhạy cảm với Memory (`WeakHashMap` / Memory Cache). |
| **Weak Reference** (`WeakReference<T>`) | GC thu gom **ngay trong đợt GC tiếp theo**, bất kể bộ nhớ còn dư hay không. | Dùng trong `WeakHashMap`, tránh Memory Leak trong `ThreadLocal`. |
| **Phantom Reference** (`PhantomReference<T>`) | Đã bị hủy hoàn toàn, chờ giải phóng bộ nhớ. | Thay thế phương thức `finalize()` để giải phóng tài nguyên Native. |

---

## 📌 Summary Sheet Chặng 5

1. **Heap vs Metaspace:** Heap chứa Objects (Eden, S0/S1, Old). Metaspace nằm ở Native Memory chứa Class Metadata.
2. **Minor vs Full GC:** Minor GC dọn Young Gen (nhanh); Full GC dọn toàn bộ Heap + Metaspace (STW lâu).
3. **5 Causes of Memory Leak:** Biến `static` phình to, Quên đóng Stream/Connection, HashMap Key thiếu `hashCode()`, Non-static Inner Class, Unregistered Listener.
4. **References:** Strong (Bình thường) $\rightarrow$ Soft (Thu gom khi thiếu RAM) $\rightarrow$ Weak (Thu gom ngay đợt GC tới) $\rightarrow$ Phantom.

---
*Tài liệu này giúp bạn tự tin làm chủ toàn bộ chủ đề JVM & Memory Management!*
