# CẨM NANG PHỎNG VẤN JAVA DEVELOPER (2-3 YOE) & BỘ CÂU HỎI THỰC CHUYẾN

Tài liệu này bao gồm **Phần 1: Bộ 12 câu hỏi phỏng vấn thực chiến chuyên sâu dựa trên CV của Trần Xuân Duy** và **Phần 2: Bộ câu hỏi mở rộng theo Tech Stack (Java Core, Spring Boot, SQL Server, Redis, Docker)** kèm câu trả lời chi tiết chuẩn Senior / Tech Lead.

---

# PHẦN 1: BỘ 12 CÂU HỎI PHỎNG VẤN THỰC CHUYẾN DỰA TRÊN CV TRẦN XUÂN DUY

---

### ❓ Câu 1: Em mô tả cụ thể câu query bị chậm > 1 phút ở dự án VNA - MCAB là gì? Tại sao em lại chọn giải pháp Reverse Indexing thay vì B-Tree Index thông thường?
* **Bối cảnh thực tế:** Trong dự án VNA - MCAB, hệ thống quản lý kho số may mắn phục vụ người dùng tìm kiếm các chuỗi số đuôi mong muốn (ví dụ tìm số kết thúc bằng `8888` hoặc `7979`).
* **Nguyên nhân chậm:** Khi người dùng tìm chuỗi số đuôi, câu lệnh SQL dạng `WHERE phone_number LIKE '%8888'` khiến B-Tree Index thông thường bị **vô hiệu hóa hoàn toàn**. SQL Server buộc phải thực hiện **Table Scan / Index Scan** toàn bộ hàng triệu dòng dữ liệu, dẫn đến CPU spike và query kéo dài trên 1 phút.
* **Giải pháp Reverse Indexing:**
  1. Thêm một cột lưu chuỗi đảo ngược: `reversed_phone_number` (ví dụ `0988888888` $\rightarrow$ `8888888890`).
  2. Tạo **B-Tree Non-Clustered Index** trên cột `reversed_phone_number`.
  3. Khi query số đuôi `8888`, chuyển câu lệnh thành: `WHERE reversed_phone_number LIKE '8888%'`.
  4. Lúc này toán tử `LIKE '8888%'` cho phép SQL Server thực hiện **Index Seek** nhảy thẳng đến vùng dữ liệu cần lấy $\rightarrow$ **Thời gian giảm từ 60 giây xuống dưới 5 giây** (thực tế chỉ còn vài milisecond).

---

### ❓ Câu 2: Em đã triển khai Table Partitioning trên SQL Server như thế nào? Làm sao để SQL Server chỉ quét đúng Partition cần tìm (Partition Elimination)?
* **Cách triển khai trên SQL Server:**
  1. **Tạo Partition Function:** Định nghĩa quy tắc phân vùng dữ liệu dựa trên Partition Key (ví dụ phân vùng theo khoảng ngày tạo `created_date` hoặc dải ID số may mắn).
  2. **Tạo Partition Scheme:** Ánh xạ các Partition vào các Filegroup bộ nhớ trên ổ đĩa.
  3. **Tạo Bảng trên Partition Scheme:** Khai báo Partition Key làm một phần của Primary Key / Clustered Index.
* **Cơ chế Partition Elimination:**
  * Để SQL Server không scan toàn bộ các Partition, mọi câu lệnh `SELECT`, `UPDATE`, `DELETE` bắt buộc phải chứa **Partition Key trong mệnh đề `WHERE`** (ví dụ `WHERE created_date >= '2025-07-01' AND created_date < '2025-08-01'`).
  * SQL Server Query Engine sẽ đọc Partition Function, xác định đúng Partition ID cần truy vấn và **bỏ qua (eliminate) hoàn toàn các Partition còn lại**.

---

### ❓ Câu 3: Ngoài Indexing và Partitioning, em đã dùng công cụ gì để chẩn đoán SQL Server Performance? Em xử lý bài toán Parameter Sniffing bao giờ chưa?
* **Công cụ chẩn đoán:**
  * **Actual Execution Plan:** Xem chi phí Operator (Table Scan vs Index Seek, Nested Loops vs Hash Match, Warnings Spill TempDB).
  * **sys.dm_exec_query_stats & sys.dm_exec_requests:** Query các Dynamic Management Views (DMVs) để tìm top các query tiêu tốn CPU và I/O nhất trên Production.
  * **SQL Server Profiler / Extended Events:** Ghi log các query chạy quá 1.000ms.
* **Xử lý Parameter Sniffing:**
  * *Hiện tượng:* SQL Server cache lại Execution Plan tối ưu cho lần chạy đầu tiên với tham số A, nhưng plan đó lại rất tệ khi chạy với tham số B.
  * *Cách xử lý:* Dùng gợi ý `OPTION (RECOMPILE)` cho query phức tạp, hoặc dùng `OPTION (OPTIMIZE FOR (@param UNKNOWN))` để SQL Server tạo plan trung bình cho mọi tham số.

---

### ❓ Câu 4: Trong bài toán Đặt vé máy bay (Booking flow) hoặc Giữ số may mắn, nếu 1.000 người dùng bấm nút cùng 1 millisecond, em xử lý bài toán chống trùng lặp / Overbooking như thế nào?
* **Giải pháp 3 Lớp Chắc Chắn:**
  1. **Lớp 1 - Distributed Lock với Redis (Redisson):** Trước khi vào DB, Thread sẽ thử lấy Lock trên Redis theo key `lock:lucky_number:{number_id}` với thời gian chờ short timeout (3s). Chỉ 1 luồng lấy được Lock mới được đi tiếp vào DB, các luồng khác bị từ chối ngay ở Layer API.
  2. **Lớp 2 - Optimistic Locking (Kho báu ít tranh chấp):** Thêm trường `@Version` vào Entity. Khi update: `UPDATE lucky_number SET status = 'SOLD', version = version + 1 WHERE id = 1 AND version = 5`. Nếu luồng khác đã sửa làm `version` tăng lên 6, Hibernate ném ra `OptimisticLockException` $\rightarrow$ Bắt ngoại lệ và báo cho client "Số đã được giữ".
  3. **Lớp 3 - Database Constraint (Chốt chặn cuối cùng):** Tạo `UNIQUE INDEX` trên cột `(flight_id, seat_number)` hoặc `(number_id, status)`. Dù code Java có bị lọt luồng thì SQL Server cũng sẽ từ chối chèn trùng dữ liệu.

---

### ❓ Câu 5: Trong luồng Booking gồm 4 bước (Giữ chỗ DB -> Trừ tiền -> Xuất vé -> Gửi Mail), em quản lý `@Transactional` trong Spring Boot như thế nào?
* **Nguyên tắc vàng:** **Không bao giờ đưa các tác vụ I/O chậm (Gửi Mail, Gọi API bên thứ 3) vào bên trong `@Transactional`** vì sẽ giữ Connection DB quá lâu gây kiệt DB Pool!
* **Cách triển khai chuẩn:**
  1. Tách phương thức DB thành `@Transactional`: Chỉ thực hiện Giữ chỗ và tạo Order record trong DB (thao tác nhanh < 10ms).
  2. Bước Gửi Mail / Xuất vé: Thực hiện ngoài Transaction.
  3. Sử dụng **Spring Event (`ApplicationEventPublisher`)** kết hợp với **`@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`**: Chỉ khi DB commit thành công, Event mới được kích hoạt.
  4. Hàm xử lý Event được đánh dấu **`@Async`** để gửi mail bất đồng bộ trên luồng riêng, không bắt client phải chờ.

---

### ❓ Câu 6: Khi tái thiết kế (Re-architected) RESTful API luồng Booking, em quản lý API Versioning, Error Format và Idempotency Key như thế nào?
* **API Versioning:** Sử dụng URI Versioning (ví dụ `/api/v1/bookings` $\rightarrow$ `/api/v2/bookings`) để đảm bảo không phá hỏng (break) các ứng dụng mobile/client cũ đang chạy.
* **Error Response Format Chuẩn:** Định nghĩa cấu trúc `ApiResponse<T>` thống nhất cho toàn bộ hệ thống (gồm `code`, `message`, `timestamp`, `details`). Sử dụng `@RestControllerAdvice` và `@ExceptionHandler` để bắt tất cả các ngoại lệ (`BusinessException`, `ValidationException`).
* **Idempotency Key (Chống trừ tiền / tạo đơn trùng):** Client gửi kèm header `X-Idempotency-Key: <UUID>`. Backend lưu Key này vào Redis với TTL 24h. Nếu nhận được request có Key đã tồn tại trong Redis, Backend lập tức trả về kết quả cũ mà không thực hiện lại logic booking.

---

### ❓ Câu 7: Em xử lý vấn đề N+1 Query trong Spring Data JPA như thế nào ở dự án VNA?
* **Hiện tượng:** Khi query 100 `Customer`, JPA phát sinh 1 query lấy danh sách `Customer` và 100 query con để lấy `BookingHistory` của từng customer ($1 + N$ queries).
* **3 Cách xử lý chuẩn Senior:**
  1. **Dùng `JOIN FETCH` trong JPQL:** `@Query("SELECT c FROM Customer c JOIN FETCH c.bookingHistories WHERE c.status = :status")`.
  2. **Dùng `@EntityGraph`:** `@EntityGraph(attributePaths = {"bookingHistories"})` trên Repository method.
  3. **Dùng DTO Projection:** Viết query chỉ lấy đúng các trường cần thiết ra DTO mà không load toàn bộ Managed Entity.

---

### ❓ Câu 8: Các Spring Bean `@Service` mặc định có Scope là Singleton. Vậy nếu 100 request cùng gọi vào Service Bean đó thì có bị xung đột dữ liệu (Thread-Safety) không?
* **Trả lời:** Spring Bean Singleton **HOÀN TOÀN THREAD-SAFE** nếu được thiết kế dạng **Stateless (Không lưu trạng thái)**.
* **Giải thích:** 100 Request tương ứng với 100 luồng (Threads) chạy song song. Mỗi luồng có bộ nhớ **Stack** riêng để chứa biến cục bộ (Local variables).
* **Cạm bẫy cần tránh:** Tuyệt đối không khai báo biến Instance có thể thay đổi (Mutable State) trong `@Service` (ví dụ `private int requestCount = 0;`). Tất cả các dependency phải là `private final` và được tiêm (Inject) qua Constructor.

---

### ❓ Câu 9: Trong CV em có ghi Redis. Em đã áp dụng Redis cho bài toán cụ thể nào và xử lý bài toán Cache Invalidation / Cache Stampede ra sao?
* **Bài toán thực tế:** Cache danh sách các mẫu số may mắn (Lucky Number Patterns) và thông tin chuyến bay thường xuyên được tra cứu.
* **Cache Invalidation Strategy:** Áp dụng mô hình **Cache-Aside (Lazy Loading)**.
  * *Đọc:* Tìm trong Redis $\rightarrow$ Nếu có (Hit) thì trả về. Nếu không (Miss) $\rightarrow$ Đọc từ SQL Server $\rightarrow$ Ghi vào Redis với TTL (ví dụ 1 tiếng) $\rightarrow$ Trả về.
  * *Ghi/Sửa:* Khi Admin cập nhật kho số $\rightarrow$ Cập nhật SQL Server trước $\rightarrow$ Xóa key (Evict) trong Redis.
* **Xử lý Cache Stampede (Nhiều luồng cùng query DB khi Cache hết hạn):** Dùng **Mutex Lock (Redis Lock)**. Luồng đầu tiên gặp Cache Miss sẽ lấy Lock để query DB và update Cache, các luồng khác tạm chờ 50ms rồi đọc lại từ Cache.

---

### ❓ Câu 10: Nếu ứng dụng Spring Boot trên Production bị treo hoặc CPU spike lên 100%, em sẽ dùng những lệnh Linux và công cụ Java nào để tìm ra dòng code gây nghẽn?
* **Quy trình 4 bước chẩn đoán nhanh:**
  1. **Lệnh `top -c` hoặc `htop`:** Tìm Process ID (`PID`) của Java đang ngốn CPU (ví dụ `PID = 1234`).
  2. **Lệnh `top -Hp 1234`:** Tìm Thread ID (`TID`) đang chiếm CPU cao nhất (ví dụ `TID = 5678`).
  3. **Chuyển TID sang Hex:** `printf "%x\n" 5678` $\rightarrow$ ra chuỗi Hex (ví dụ `0x162e`).
  4. **Xuất Thread Dump bằng `jstack`:** `jstack 1234 > thread_dump.txt`, sau đó `grep -A 30 "0x162e" thread_dump.txt` để chỉ đích danh tên Class và Dòng Code đang bị lặp vô hạn hoặc nghẽn Lock!

---

### ❓ Câu 11: Em viết `Dockerfile` cho ứng dụng Spring Boot như thế nào? Làm sao để tối ưu kích thước Docker Image và quản lý config an toàn?
* **Kỹ thuật Multi-Stage Build:**
  ```dockerfile
  # Stage 1: Build JAR với Maven
  FROM maven:3.9-eclipse-temurin-17-alpine AS builder
  WORKDIR /app
  COPY pom.xml .
  COPY src ./src
  RUN mvn clean package -DskipTests

  # Stage 2: Runtime Image nhẹ gọn (Chỉ lấy file JAR)
  FROM eclipse-temurin:17-jre-alpine
  WORKDIR /app
  COPY --from=builder /app/target/*.jar app.jar
  EXPOSE 8080
  ENTRYPOINT ["java", "-jar", "app.jar"]
  ```
* **Kết quả:** Giảm kích thước Image từ $>800MB$ xuống chỉ còn $\sim 180MB$.
* **Bảo mật Config:** Không bao giờ hardcode mật khẩu vào `application.yml`. Sử dụng biến môi trường (Environment Variables) truyền từ Docker Compose / Kubernetes Secrets (`SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}`).

---

### ❓ Câu 12: Em áp dụng AI-Assisted Coding và Prompt Engineering như thế nào trong công việc hàng ngày? Em kiểm soát chất lượng mã nguồn ra sao?
* **Ứng dụng thực tế:**
  * Sử dụng GitHub Copilot / ChatGPT để tự động sinh ra dữ liệu giả (Mock Data), viết các lớp DTO, Mapper, và tạo nhanh khung Unit Test (`JUnit 5 / Mockito`).
  * Dùng Prompt Engineering để nhờ AI phân tích độ phức tạp thuật toán và refactor các đoạn code lặp rườm rà.
* **Kiểm soát chất lượng & Bảo mật:**
  * **Quy tắc an toàn dữ liệu:** Không bao giờ paste mã nguồn có chứa bí mật thương mại, IP công ty, hay mật khẩu/API Key lên các công cụ AI công cộng.
  * **Code Review:** Mọi mã nguồn do AI sinh ra bắt buộc phải được đọc hiểu bản chất, chạy thử Unit Test và qua bước Peer Code Review trước khi merge vào nhánh `main`.

---
---

# PHẦN 2: BỘ CÂU HỎI MỞ RỘNG THEO TECH STACK (CÓ ĐÁP ÁN CHI TIẾT)

---

## 1. JAVA CORE (NỀN TẢNG KIẾN TRÚC)

### ❓ 1.1. Phân biệt `==` và `.equals()`? Tại sao lớp `String` lại Bất biến (Immutable)?
* **`==` vs `.equals()`:**
  * `==`: So sánh địa chỉ vùng nhớ (Reference) giữa 2 biến.
  * `.equals()`: So sánh giá trị nội dung logic giữa 2 đối tượng (đã được override từ `Object`).
* **Tại sao `String` lại Immutability?**
  1. **String Pool:** Cho phép JVM tái sử dụng các chuỗi giống nhau trong bộ nhớ Heap để tiết kiệm RAM.
  2. **Bảo mật (Security):** Chuỗi `String` được dùng lưu Username, Password, URL DB. Nếu String sửa đổi được, kẻ tấn công có thể thay đổi dữ liệu ngầm.
  3. **Thread-Safety:** Chuỗi bất biến an toàn tuyệt đối khi dùng chung giữa nhiều luồng mà không cần khóa (`synchronized`).

### ❓ 1.2. Phân biệt `HashMap` và `ConcurrentHashMap`?
* `HashMap`: Không thread-safe, hiệu năng cao trong đơn luồng. Trường hợp đụng độ xấu nhất Java 8+ là $O(\log N)$ nhờ Red-Black Tree.
* `ConcurrentHashMap`: Thread-safe trong đa luồng. Không dùng khóa toàn bộ Map. Dùng **CAS** cho bucket rỗng + **`synchronized` trên Bucket Head Node** khi chèn. Thao tác đọc `get()` không tốn lock. Cấm `null` key và `null` value.

### ❓ 1.3. Phân biệt `Stack` và `Heap` memory? Lỗi `StackOverflowError` vs `OutOfMemoryError`?
* **Stack:** Lưu biến cục bộ, tham chiếu đối tượng và lời gọi phương thức (LIFO). Mỗi Thread có Stack riêng. Đầy Stack ném ra `StackOverflowError` (do đệ quy vô hạn).
* **Heap:** Lưu trữ tất cả Objects và Arrays. Dùng chung cho toàn ứng dụng, do GC quản lý. Đầy Heap ném ra `OutOfMemoryError` (do leak bộ nhớ hoặc cấp phát quá lớn).

---

## 2. SPRING BOOT & JPA (FRAMEWORK CORE)

### ❓ 2.1. Phân biệt `@Component`, `@Service`, `@Repository` và `@Controller`?
* Tất cả đều là các Annotation đại diện cho **Spring Managed Beans** (được Spring IoC Container quản lý).
* `@Component`: Annotation chung cho bất kỳ Spring Bean nào.
* `@Service`: Đánh dấu lớp chứa Logic nghiệp vụ (Business Logic Layer).
* `@Repository`: Đánh dấu lớp truy vấn dữ liệu (DAO Layer), có thêm tính năng tự động chuyển đổi các SQL Exceptions thành Spring `DataAccessException`.
* `@Controller` / `@RestController`: Đánh dấu lớp tiếp nhận Request (Presentation Layer). `@RestController` = `@Controller` + `@ResponseBody`.

### ❓ 2.2. IoC (Inversion of Control) và DI (Dependency Injection) là gì? Các cách Inject Bean?
* **IoC:** Nguyên lý đảo ngược quyền kiểm soát. Thay vì lập trình viên tự `new` đối tượng, việc khởi tạo và quản lý vòng đời đối tượng được giao cho Spring Container.
* **DI:** Thiết kế mẫu triển khai IoC. Spring tự động tiêm (inject) các phụ thuộc vào class.
* **3 Cách Inject:** Field Injection (`@Autowired`), Setter Injection, và **Constructor Injection (Cách khuyên dùng - giúp code immutability, dễ viết Unit Test với Mockito)**.

### ❓ 2.3. Các Propagation (Tính lan truyền) phổ biến trong `@Transactional`?
* `REQUIRED` (Mặc định): Nếu đã có Transaction thì tham gia vào, nếu chưa có thì tạo Transaction mới.
* `REQUIRES_NEW`: Luôn luôn tạo một Transaction mới độc lập, tạm dừng Transaction cũ (nếu có). Dùng cho việc ghi Log độc lập không bị rollback theo luồng chính.
* `NESTED`: Tạo một Savepoint bên trong Transaction hiện tại.

---

## 3. SQL SERVER (ACID, TRANSACTIONS & INDEXING)

### ❓ 3.1. Trình bày 4 tính chất ACID của Database Transaction?
1. **Atomicity (Tính nguyên tố):** Tất cả các câu lệnh trong Transaction cùng thành công hoặc cùng thất bại (All or Nothing).
2. **Consistency (Tính nhất quán):** Dữ liệu phải tuân thủ đúng tất cả các ràng buộc (Constraints, FK, PK) trước và sau khi Transaction chạy.
3. **Isolation (Tính cô lập):** Các Transaction chạy đồng thời không được can thiệp hoặc làm sai lệch dữ liệu của nhau.
4. **Durability (Tính bền vững):** Khi Transaction đã Commit, dữ liệu chắc chắn được ghi vĩnh viễn xuống ổ đĩa ngay cả khi server mất điện đột ngột.

### ❓ 3.2. Bốn cấp độ cô lập Transaction (Isolation Levels) và các hiện tượng đọc sai dữ liệu?

| Isolation Level | Dirty Read | Non-Repeatable Read | Phantom Read |
| :--- | :---: | :---: | :---: |
| **Read Uncommitted** | ⚠️ Có bị | ⚠️ Có bị | ⚠️ Có bị |
| **Read Committed** (Mặc định SQL Server) |  Khỏi | ⚠️ Có bị | ⚠️ Có bị |
| **Repeatable Read** |  Khỏi |  Khỏi | ⚠️ Có bị |
| **Serializable** |  Khỏi |  Khỏi |  Khỏi |

* **Dirty Read:** Đọc phải dữ liệu chưa được Commit của Transaction khác (sau đó bị Rollback).
* **Non-Repeatable Read:** Trong cùng 1 Transaction, đọc 1 dòng dữ liệu 2 lần ra 2 kết quả khác nhau do Transaction khác vừa `UPDATE`.
* **Phantom Read:** Trong cùng 1 Transaction, query danh sách 2 lần ra số lượng dòng khác nhau do Transaction khác vừa `INSERT`.

### ❓ 3.3. Phân biệt Clustered Index và Non-Clustered Index trong SQL Server?
* **Clustered Index:** Sắp xếp và lưu trữ **dữ liệu thực tế** của bảng theo thứ tự của Index (như cuốn từ điển). Mỗi bảng chỉ có **DUY NHẤT 1** Clustered Index (thường là Primary Key).
* **Non-Clustered Index:** Lưu trữ cấu trúc chỉ mục riêng biệt chứa Key và con trỏ (Row Locator / Clustered Key) trỏ về bảng dữ liệu thực tế (như trang mục lục cuối sách). Một bảng có thể có **NHIỀU** Non-Clustered Index.

---

## 4. REDIS (CACHING & DISTRIBUTED LOCK)

### ❓ 4.1. Redis là gì? Tại sao Redis lại đạt tốc độ đọc/ghi cực nhanh hàng trăm ngàn req/s?
* **Redis** là hệ thống lưu trữ dữ liệu dạng Key-Value In-Memory (lưu hoàn toàn trên RAM).
* **Lý do siêu nhanh:**
  1. Dữ liệu nằm trực tiếp trên **RAM** (không phải đọc đĩa I/O).
  2. Kiến trúc **Single-Threaded Event Loop** kết hợp với **I/O Multiplexing** (không tốn chi phí Context Switching giữa các luồng).
  3. Cấu trúc dữ liệu được tối ưu hóa cực tốt bằng C (SDS, ZipList, SkipList, HashTable).

### ❓ 4.2. Phân biệt Cache Avalanche, Cache Penetration và Cache Breakdown?

| Hiện tượng | Bản chất vấn đề | Giải pháp khắc phục |
| :--- | :--- | :--- |
| **Cache Avalanche (Tuyết lở)** | Hàng loạt Key cùng hết hạn TTL tại một thời điểm $\rightarrow$ Request tràn xuống làm sập DB. | Thêm thời gian ngẫu nhiên (Random Jitter) vào TTL (ví dụ $60m + random(1..5m)$). |
| **Cache Penetration (Xuyên thấu)** | Client cố tình query Key **không tồn tại cả trong Cache lẫn DB** $\rightarrow$ Request đâm thẳng xuống DB. | Lưu Key rỗng vào Cache với TTL ngắn (`set("key", NULL, 5m)`) hoặc dùng **Bloom Filter**. |
| **Cache Breakdown (Sập điểm)** | Một Key **Hot-spot** (truy cập cực nhiều) bị hết hạn đúng lúc cao điểm $\rightarrow$ Hàng ngàn luồng cùng đâm xuống DB. | Dùng Mutex Lock (Redis Lock) hoặc cài đặt **Logical Expiration** (gia hạn ngầm). |

---

## 5. DOCKER (CONTAINERIZATION & DEPLOYMENT)

### ❓ 5.1. Phân biệt Docker Image và Docker Container?
* **Docker Image:** Là một Template chỉ đọc (Read-only) chứa toàn bộ mã nguồn, thư viện, môi trường chạy và file cấu hình ứng dụng (như bản thiết kế).
* **Docker Container:** Là một thể hiện (Instance) đang thực thi của Docker Image. Nó tạo ra một môi trường cô lập có lớp ổ đĩa ghi được (Read-Write Layer).

### ❓ 5.2. Phân biệt Bind Mount và Volume trong Docker Data Persistence?
* **Bind Mount:** Ánh xạ trực tiếp một đường dẫn thư mục cố định từ máy Host vào bên trong Container (`-v /var/log/app:/app/logs`). Phụ thuộc vào cấu trúc tệp của máy Host.
* **Volume:** Do Docker quản lý hoàn toàn nằm trong vùng nhớ riêng của Docker (`/var/lib/docker/volumes/`). An toàn hơn, độc lập với hệ điều hành Host và khuyên dùng cho Database (SQL Server, Postgres).

---
*Tài liệu cẩm năng phỏng vấn này giúp bạn tự tin chinh phục mọi buổi phỏng vấn Java Developer!*
