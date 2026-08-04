# Bộ Câu Hỏi Phỏng Vấn Thực Tế - Chặng 3: Multithreading & Concurrency

Tài liệu này tổng hợp các **câu hỏi phỏng vấn thực tế** kèm **gợi ý trả lời chuẩn Senior** cho vị trí Java Developer (2 năm kinh nghiệm trở lên) về Concurrency & Multithreading.

---

## ❓ Câu 1: Phân biệt `volatile` và `synchronized`? Tại sao khai báo `private volatile int count = 0;` rồi gọi `count++` lại KHÔNG Thread-safe?
* **Mục đích hỏi:** Kiểm tra kiến thức về Visibility vs Atomicity.
* **Gợi ý trả lời chuẩn:**
  * `volatile` chỉ đảm bảo tính **Visibility** (ghi trực tiếp xuống Main Memory) và **Prevent Reordering** (Memory Barrier). Nó **KHÔNG** đảm bảo tính nguyên tử (**Atomicity**).
  * `synchronized` đảm bảo cả 3 tính chất: **Visibility**, **Atomicity**, và **Mutual Exclusion** (Khóa tương hỗ).
  * Phép tính `count++` thực chất gồm 3 bước ở cấp độ CPU: Read $\rightarrow$ Modify $\rightarrow$ Write. Hai luồng cùng đọc `count = 0` đồng thời sẽ cùng ghi lại `count = 1` $\rightarrow$ Gây lỗi Race Condition.
  * **Giải pháp:** Dùng `AtomicInteger` (giải thuật CAS) hoặc bao quanh bởi block `synchronized`.

---

## ❓ Câu 2: Trình bày chính xác luồng xử lý của `ThreadPoolExecutor` khi một Task mới được submit?
* **Mục đích hỏi:** Đây là câu hỏi kinh điển đo độ sâu về Thread Pool. 90% ứng viên trả lời sai bước đệm Queue.
* **Gợi ý trả lời chuẩn:**
  1. Nếu số luồng đang chạy $< \text{corePoolSize} \rightarrow$ Tạo luồng mới xử lý task ngay lập tức.
  2. Nếu số luồng $\ge \text{corePoolSize} \rightarrow$ Đưa task vào hàng chờ **`workQueue`**.
  3. Nếu `workQueue` bị ĐẦY và số luồng $< \text{maximumPoolSize} \rightarrow$ Tạo thêm luồng tạm thời (non-core thread) để xử lý.
  4. Nếu `workQueue` ĐẦY và số luồng $= \text{maximumPoolSize} \rightarrow$ Kích hoạt quy tắc từ chối **`RejectedExecutionHandler`**.

---

## ❓ Câu 3: Tại sao trong code Production lại NÊN HẠN CHẾ sử dụng `Executors.newFixedThreadPool()` và `Executors.newCachedThreadPool()`?
* **Mục đích hỏi:** Đánh giá kinh nghiệm thực chiến xử lý sự cố OutOfMemoryError (OOM) trong môi trường tải cao.
* **Gợi ý trả lời chuẩn:**
  * `Executors.newFixedThreadPool(n)` sử dụng `LinkedBlockingQueue` không giới hạn dung lượng (**Unbounded Queue**). Khi bị ngập request, Queue phình to vô hạn gây **`OutOfMemoryError` (Heap Space)**.
  * `Executors.newCachedThreadPool()` thiết lập `maximumPoolSize = Integer.MAX_VALUE`. Khi request tăng đột biến, hệ thống cố tạo hàng chục ngàn Thread $\rightarrow$ Gây **`OutOfMemoryError` / Kiệt tài nguyên hệ thống (OS Threads Limit)**.
  * **Giải pháp Senior:** Tự khởi tạo `ThreadPoolExecutor` với tham số `corePoolSize`, `maximumPoolSize` hợp lý và dùng **Bounded Queue** (giới hạn kích thước queue).

---

## ❓ Câu 4: Deadlock là gì? Làm thế nào để phát hiện và phòng tránh Deadlock trong hệ thống Production?
* **Mục đích hỏi:** Kiểm tra kỹ năng Debug & Thiết kế hệ thống đa luồng.
* **Gợi ý trả lời chuẩn:**
  * **Deadlock** xảy ra khi hai hay nhiều luồng bị treo vĩnh viễn vì mỗi luồng đều đang giữ tài nguyên mà luồng kia đang chờ.
  * **Cách phát hiện:** Sử dụng công cụ chẩn đoán Thread Dump như `jstack <pid>`, `jconsole`, `VisualVM` hoặc Prometheus/Grafana metrics. `jstack` sẽ tự động hiển thị đoạn "Found 1 deadlock".
  * **Cách phòng tránh:**
    1. **Lock Ordering Rule (Quy tắc thứ tự lấy khóa):** Đảm bảo tất cả các luồng luôn lấy khóa theo đúng một thứ tự cố định (ví dụ: luôn lấy Lock A trước Lock B).
    2. **Lock Timeout:** Dùng `ReentrantLock.tryLock(timeout)` thay vì `synchronized` để nếu không lấy được khóa trong thời gian cho phép thì nhả khóa ra và thử lại sau.

---

## ❓ Câu 5: Phân biệt `CompletableFuture.thenApply()` và `CompletableFuture.thenCompose()`?
* **Mục đích hỏi:** Kiểm tra kiến thức Asynchronous programming trong Java 8+.
* **Gợi ý trả lời chuẩn:**
  * `thenApply()` tương tự như `map()` trong Stream API. Nó nhận kết quả của Stage trước và trả về một giá trị đã biến đổi `CompletableFuture<U>`.
  * `thenCompose()` tương tự như `flatMap()` trong Stream API. Dùng khi hàm biến đổi bản thân nó cũng trả về một `CompletableFuture<U>` khác, giúp phẳng hóa (flatten) 2 Stage lồng nhau thay vì bị kiểu `CompletableFuture<CompletableFuture<U>>`.

---
*Tài liệu này giúp bạn tự tin làm chủ toàn bộ câu hỏi phỏng vấn Concurrency!*
