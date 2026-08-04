# Multithreading, Concurrency & Thread Pools Trong Java

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Đa luồng (Multithreading), Xử lý đồng thời (Concurrency), Thread Pools và CompletableFuture** dành cho lập trình viên Java 2 năm kinh nghiệm chuẩn bị phỏng vấn và thi chứng chỉ OCP.

---

## Phần 1: Thread Fundamentals & Vòng Đời Luồng

### 1. Phân biệt `Runnable` vs `Callable` vs `Thread`

| Tiêu chí | `Thread` | `Runnable` | `Callable<V>` |
| :--- | :--- | :--- | :--- |
| **Bản chất** | Lớp biểu diễn một luồng execution. | Functional Interface (`void run()`). | Functional Interface (`V call()`). |
| **Giá trị trả về** | Không có. | `void` (không có giá trị trả về). | Trả về đối tượng kiểu `V`. |
| **Xử lý Exception** | Phải xử lý bằng `try-catch` trong `run()`. | Phải xử lý bằng `try-catch` trong `run()`. | Cho phép **ném ra Checked Exception**. |
| **Kết hợp với ThreadPool** | Khó quản lý. | Thực thi được qua `submit()` / `execute()`. | Thực thi qua `submit()`, trả về `Future<V>`. |

---

### 2. Sáu Trạng Thái Vòng Đời Luồng (Thread States)

1. **`NEW`**: Thread được tạo mới bằng `new Thread()`, chưa gọi `.start()`.
2. **`RUNNABLE`**: Đang chạy hoặc sẵn sàng chờ CPU cấp Time-slice để chạy.
3. **`BLOCKED`**: Đang chờ lấy Monitor Lock (từ từ khóa `synchronized`) để vào block/method.
4. **`WAITING`**: Đang chờ luồng khác phát tín hiệu (`Object.wait()`, `Thread.join()`, `LockSupport.park()`).
5. **`TIMED_WAITING`**: Chờ có thời hạn (`Thread.sleep(ms)`, `Object.wait(timeout)`).
6. **`TERMINATED`**: Đã hoàn thành công việc hoặc bị ném ra Exception không được xử lý.

---

## Phần 2: Từ Khóa `volatile` và Lớp `Atomic`

### 1. Từ khóa `volatile`

Một câu hỏi phỏng vấn kinh điển: **"`volatile` giải quyết vấn đề gì và tại sao nó KHÔNG đảm bảo tính nguyên tử (Atomicity)?"**

#### Cơ chế hoạt động:
* **Visibility (Tính nhìn thấy):** Ghi trực tiếp giá trị của biến vào **Main Memory** thay vì giữ ở CPU Cache local của từng core CPU. Giúp tất cả các Thread khác nhìn thấy ngay lập tức giá trị mới nhất.
* **Prevent Instruction Reordering (Chống đảo thứ tự lệnh):** Tạo ra một rào chắn bộ nhớ (Memory Barrier), ngăn Compiler/CPU sắp xếp lại thứ tự đọc/ghi quanh biến `volatile`.

#### ⚠️ Tại sao `volatile` KHÔNG thay thế được `synchronized`?
```java
private volatile int count = 0;

public void increment() {
    count++; // ❌ KHÔNG THREAD-SAFE!
}
```
* Phép tính `count++` gồm **3 bước độc lập ở cấp độ CPU**:
  1. Read value từ bộ nhớ.
  2. Modify value (`value + 1`).
  3. Write value lại bộ nhớ.
* Nếu 2 luồng cùng thực hiện `count++` đồng thời, cả hai có thể cùng đọc `count = 0` và cùng ghi đè giá trị `1` $\rightarrow$ Mất dữ liệu (Race Condition).

---

### 2. Các Lớp `Atomic` & Thuật Toán CAS (Compare-And-Swap)

Để giải quyết vấn đề `count++` mà không muốn tốn chi phí khóa (`synchronized`), Java cung cấp các lớp `Atomic` (`AtomicInteger`, `AtomicLong`, `AtomicReference`).

* **Giải thuật CAS (Compare-And-Swap):** Là một câu lệnh nguyên tử cấp phần cứng CPU.
* **Cơ chế:** CPU so sánh giá trị hiện tại ở bộ nhớ với giá trị kỳ vọng ($V_{expected}$). Nếu bằng nhau, nó mới ghi giá trị mới ($V_{new}$). Nếu không bằng nhau (do luồng khác vừa sửa), nó sẽ thử lại (retry loop) cho đến khi thành công.
* **Ưu điểm:** **Lock-Free**, hiệu năng cực cao khi độ tranh chấp (contention) ở mức thấp và trung bình.

---

## Phần 3: Đồng Bộ Luồng với `ReentrantLock`

So với từ khóa `synchronized` (Implicit Lock), lớp **`ReentrantLock`** (Explicit Lock) cung cấp các tính năng nâng cao:

1. **Khả năng `tryLock(timeout)`:** Thử lấy khóa trong khoảng thời gian chỉ định, nếu không lấy được thì bỏ qua chứ không bị treo vĩnh viễn (`BLOCKED`).
2. **Khả năng `lockInterruptibly()`:** Cho phép hủy việc chờ khóa nếu luồng bị ngắt (`interrupt`).
3. **Chính sách công bằng (Fairness Policy):** `new ReentrantLock(true)` đảm bảo luồng nào chờ trước sẽ được lấy khóa trước (FIFO), tránh tình trạng ngợp luồng (Thread Starvation).

```java
ReentrantLock lock = new ReentrantLock();

public void doWork() {
    lock.lock(); // Lấy khóa
    try {
        // Critical Section (Mã nguồn cần bảo vệ)
    } finally {
        lock.unlock(); // ⚠️ BẮT BUỘC phải giải phóng khóa trong block finally!
    }
}
```

---

## Phần 4: Thread Pools & `ThreadPoolExecutor` (CỰC KỲ QUAN TRỌNG)

Tái sử dụng các luồng để tránh chi phí đắt đỏ của việc khởi tạo và hủy `Thread` liên tục.

---

### 1. Năm Tham Số Vàng Của `ThreadPoolExecutor`

```java
ThreadPoolExecutor executor = new ThreadPoolExecutor(
    corePoolSize,     // 1. Số luồng tối thiểu luôn duy trì
    maximumPoolSize,  // 2. Số luồng tối đa cho phép tạo ra
    keepAliveTime,    // 3. Thời gian sống của luồng dư thừa khi rỗi
    TimeUnit.SECONDS, // 4. Đơn vị thời gian
    workQueue,        // 5. Hàng chờ chứa tác vụ chưa được xử lý
    handler           // 6. Quy tắc xử lý khi hàng chờ bị đầy (RejectedExecutionHandler)
);
```

### 2. Luồng Xử Lý Khi Một Tác Vụ Mới Được Submit (Thuộc lòng!)

Khi gọi `executor.submit(task)`:
1. **Bước 1:** Nếu số luồng đang chạy $< \text{corePoolSize} \rightarrow$ Tạo ngay 1 Thread mới để xử lý task.
2. **Bước 2:** Nếu số luồng $\ge \text{corePoolSize} \rightarrow$ Đưa task vào hàng chờ **`workQueue`**.
3. **Bước 3:** Nếu `workQueue` bị ĐẦY và số luồng $< \text{maximumPoolSize} \rightarrow$ Tạo thêm luồng mới (non-core thread) để xử lý.
4. **Bước 4:** Nếu `workQueue` ĐẦY và số luồng $= \text{maximumPoolSize} \rightarrow$ Kích hoạt quy tắc **`RejectedExecutionHandler`**.

---

### 3. Cạm Bẫy Từ Các Hàm Tạo Đóng Gói Phổ Biến (`Executors`)

Một câu hỏi phỏng vấn kinh điển: **"Tại sao trong code Production lại NÊN HẠN CHẾ sử dụng các hàm tiện ích của `Executors`?"**

1. **`Executors.newFixedThreadPool(n)`**: Sử dụng `LinkedBlockingQueue` không giới hạn độ dài (**Unbounded Queue**). Nếu request đến quá dồn dập, Queue phình to $\rightarrow$ Trực tiếp gây lỗi **`OutOfMemoryError` (Heap Space)**!
2. **`Executors.newCachedThreadPool()`**: Thiết lập `maximumPoolSize = Integer.MAX_VALUE`. Nếu request tăng đột biến, hệ thống tạo hàng chục ngàn Thread $\rightarrow$ Trực tiếp gây lỗi **`OutOfMemoryError` / Crashed OS** do kiệt RAM hệ thống!
3. **Lời khuyên Senior:** Luôn tự khởi tạo `ThreadPoolExecutor` với tham số `corePoolSize`, `maximumPoolSize`, và `workQueue` có kích thước giới hạn (**Bounded Queue**) cụ thể.

---

## Phần 5: Lập Trình Bất Đồng Bộ Với `CompletableFuture` (Java 8+)

`CompletableFuture` hỗ trợ lập trình Reactive / Asynchronous theo chuỗi (Pipeline) mà không làm ngắt luồng chính (Non-blocking).

```java
CompletableFuture.supplyAsync(() -> fetchUserData(userId)) // Chạy bất đồng bộ
    .thenApply(user -> calculateDiscount(user))           // Transform kết quả
    .thenAccept(discount -> sendEmail(discount))          // Tiêu thụ kết quả
    .exceptionally(ex -> {                                // Xử lý Exception
        log.error("Lỗi xử lý", ex);
        return null;
    });
```

* **`thenApply()`**: Tương tự `map()`, chuyển đổi kết quả thành giá trị mới.
* **`thenCompose()`**: Tương tự `flatMap()`, nối chuỗi các `CompletableFuture` lồng nhau.
* **`allOf()`**: Chờ tất cả các `CompletableFuture` hoàn tất song song.

---

## Phần 6: Các Sự Cố Concurrency Thường Gặp

1. **Race Condition:** Nhiều luồng cùng truy cập và sửa đổi dữ liệu dùng chung dẫn đến kết quả sai lệch.
2. **Deadlock (Khóa chết):** Luồng A giữ Lock 1 chờ Lock 2; Luồng B giữ Lock 2 chờ Lock 1. Cả hai treo vĩnh viễn.
   * *Cách phòng tránh:* Thiết lập thứ tự lấy khóa cố định (Lock Ordering Rule) hoặc dùng `tryLock(timeout)`.
3. **Starvation (Ngợp luồng):** Luồng ưu tiên thấp không bao giờ được cấp CPU để chạy.

---

## 📌 Summary Sheet Chặng 3

1. **`volatile`:** Đảm bảo Visibility & Memory Barrier, KHÔNG đảm bảo Atomicity (`count++`).
2. **`Atomic` Classes:** Dùng giải thuật **CAS (Compare-And-Swap)** cấp CPU, Lock-Free hiệu năng cao.
3. **`ThreadPoolExecutor` Steps:** Core Pool $\rightarrow$ Work Queue $\rightarrow$ Max Pool $\rightarrow$ Reject Handler.
4. **`Executors` Danger:** Tránh `newFixedThreadPool` (Queue không giới hạn) & `newCachedThreadPool` (Thread không giới hạn) gây **OOM**.
5. **`CompletableFuture`:** Lập trình bất đồng bộ Non-blocking với `thenApply`, `thenCompose`, `allOf`.

---
*Tài liệu này tổng hợp toàn bộ trọng tâm Concurrency & Thread Pools!*
