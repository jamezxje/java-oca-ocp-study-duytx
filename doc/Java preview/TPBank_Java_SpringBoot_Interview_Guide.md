# BỘ TÀI LIỆU ÔN TẬP PHỎNG VẤN JAVA SPRING BOOT (TP BANK ONSITE - 1 VÒNG ONLINE)

> **Vị trí**: Java Spring Boot Developer (Phỏng vấn 1 vòng Online - Ký HĐ chính thức)  
> **Trọng tâm**: Java Core, Spring Boot, Hibernate/JPA, RESTful API, Microservices, Oracle/MySQL, Banking Domain Context.

---

## MỤC LỤC

1. [Phần 1: Java Core & Đa luồng (Concurrency)](#phan-1-java-core--da-luong-concurrency)
2. [Phần 2: Spring Boot & Spring Ecosystem](#phan-2-spring-boot--spring-ecosystem)
3. [Phần 3: JPA & Hibernate (Database ORM)](#phan-3-jpa--hibernate-database-orm)
4. [Phần 4: RESTful API & Kiến trúc Microservices](#phan-4-restful-api--kien-truc-microservices)
5. [Phần 5: Quản trị CSDL (Oracle & MySQL)](#phan-5-quan-tri-csdl-oracle--mysql)
6. [Phần 6: Banking Domain & Kỹ thuật nâng cao (Idempotency, Distributed Transaction)](#phan-6-banking-domain--ky-thuat-nang-cao)
7. [Phần 7: Tổng hợp 30+ Câu hỏi Phỏng vấn Trực tiếp & Câu trả lời Mẫu](#phan-7-tong-hop-30-cau-hoi-phong-van)

---

## PHẦN 1: JAVA CORE & ĐA LUỒNG (CONCURRENCY)

### 1.1 OOP & Banking Domain Example
- **Encapsulation (Đóng gói)**: Giấu thuộc tính nhạy cảm (như `balance`, `pinCode`), chỉ cho phép thay đổi qua method kiểm tra hợp lệ (`deposit()`, `withdraw()`).
- **Inheritance (Kế thừa)**: Class `Account` làm lớp cha, `CheckingAccount` (tài khoản thanh toán), `SavingsAccount` (tài khoản tiết kiệm) kế thừa.
- **Polymorphism (Đa hình)**: Method `calculateInterest()` thực thi khác nhau giữa `SavingsAccount` và `CheckingAccount`.
- **Abstraction (Trừu tượng)**: Định nghĩa interface `PaymentGateway` với method `processPayment()`. Các implementation: `VNPayGateway`, `NapasGateway`.

---

### 1.2 Collections Framework (HashMap vs ConcurrentHashMap vs ArrayList)
#### Phân biệt ArrayList vs LinkedList
| Tiêu chí | ArrayList | LinkedList |
| :--- | :--- | :--- |
| **Cấu trúc dữ liệu** | Mảng động (Dynamic Array) | Danh sách liên kết đôi (Doubly Linked List) |
| **Truy cập ngẫu nhiên (`get(i)`)** | $O(1)$ | $O(n)$ |
| **Thêm/Xóa (`add/remove`)** | $O(n)$ nếu phải shift element | $O(1)$ nếu đã có reference đến Node |
| **Sử dụng Bộ nhớ** | Ít tốn bộ nhớ hơn | Tốn thêm bộ nhớ lưu `next` và `prev` pointers |

#### Cơ chế hoạt động nội tại của HashMap (Java 8+)
1. **Mảng Buckets**: HashMap dùng mảng các `Node<K,V>[] table`.
2. **Tính vị trí**: `hash = key.hashCode()`, `index = hash & (n - 1)`.
3. **Xử lý đụng độ (Collision)**:
   - Dùng **Separate Chaining** (Danh sách liên kết).
   - Từ Java 8: Khi số lượng phần tử trong 1 bucket $\ge 8$ và kích thước bảng $\ge 64$, LinkedList sẽ tự động chuyển thành **Red-Black Tree** (Cây đỏ đen) để giảm độ phức tạp tìm kiếm từ $O(n)$ xuống $O(\log n)$.
4. **HashMap vs ConcurrentHashMap**:
   - `HashMap`: Không thread-safe.
   - `Hashtable`: Thread-safe nhưng dùng `synchronized` trên toàn bộ method $\rightarrow$ hiệu năng rất thấp.
   - `ConcurrentHashMap` (Java 8+): Sử dụng **CAS (Compare-And-Swap)** và **Bucket-level Locking** (`synchronized` chỉ trên Node đầu tiên của Bucket bị ghi), nâng cao hiệu năng concurrency tối đa.

---

### 1.3 Java Multithreading & Concurrency

#### CompletableFuture & ThreadPool
Trong ứng dụng Ngân hàng, việc gọi nhiều service song song (VD: Kiểm tra số dư + Kiểm tra hạn mức + Kiểm tra danh sách đen CIC) nên dùng `CompletableFuture`:

```java
CompletableFuture<Boolean> checkBalance = CompletableFuture.supplyAsync(() -> balanceService.hasEnoughMoney(accId, amount), customThreadPool);
CompletableFuture<Boolean> checkCIC = CompletableFuture.supplyAsync(() -> cicService.isCleanHistory(customerId), customThreadPool);

CompletableFuture<Void> allChecks = CompletableFuture.allOf(checkBalance, checkCIC);
allChecks.join(); // Đợi cả 2 tác vụ hoàn tất
```

#### Memory Leak trong Java
- Nguyên nhân: Các object không còn sử dụng nhưng vẫn bị tham chiếu (Reference), dẫn đến GC không thể thu hồi.
- Trường hợp phổ biến trong Spring/Java:
  1. Sử dụng `ThreadLocal` nhưng không gọi `remove()` sau khi xử lý xong request (đặc biệt trong ThreadPool).
  2. Static Collection lưu quá nhiều dữ liệu không dọn dẹp.
  3. Không đóng resource (InputStream, DB Connection, Socket).

---

## PHẦN 2: SPRING BOOT & SPRING ECOSYSTEM

### 2.1 IoC & Dependency Injection (DI)
- **IoC (Inversion of Control)**: Đảo ngược quyền điều khiển việc khởi tạo và quản lý vòng đời của Object cho Spring Container (ApplicationContext).
- **DI (Dependency Injection)**: Mẫu thiết kế để Spring tiêm các dependency vào bean.
- **Tại sao nên dùng Constructor Injection thay vì `@Autowired` trên Field?**
  1. **Immutability**: Có thể khai báo field là `final`.
  2. **Testability**: Dễ dàng mock/unit test mà không cần dùng Spring Runner hay Reflection.
  3. **Phát hiện Circular Dependency**: Spring sẽ báo lỗi ngay khi khởi động ứng dụng (Compile/Startup time) thay vì Runtime.

```java
// RECOMMENDED
@Service
public class AccountService {
    private final AccountRepository accountRepository;

    public AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }
}
```

---

### 2.2 Spring Bean Scopes & Lifecycle
1. **Singleton** (Default): 1 instance duy nhất per Spring Container.
2. **Prototype**: Tạo 1 instance mới mỗi lần được request/inject.
3. **Request**: 1 instance per HTTP Request (chỉ dùng trong Web Context).
4. **Session**: 1 instance per HTTP Session.

#### Bean Lifecycle chính:
`Instantiate` $\rightarrow$ `Populate Properties` $\rightarrow$ `@PostConstruct` / `BeanPostProcessor` $\rightarrow$ `Bean Ready` $\rightarrow$ `@PreDestroy`.

---

### 2.3 Spring Boot Auto-Configuration
- Được kích hoạt bởi Annotation `@SpringBootApplication` (bao gồm `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`).
- Cơ chế: Đọc file `META-INF/spring.factories` (hoặc `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` ở Spring Boot 3+) để load các AutoConfiguration class kết hợp với các điều kiện `@ConditionalOnClass`, `@ConditionalOnMissingBean`, v.v.

---

## PHẦN 3: JPA & HIBERNATE (DATABASE ORM)

### 3.1 Vấn đề N+1 Query & 4 Cách Xử Lý Chi Tiết
**Định nghĩa**: Khi bạn query 1 danh sách $N$ phần tử (VD: `Customer`), và với mỗi `Customer`, Hibernate lại chạy thêm 1 câu SQL SELECT để lấy danh sách `Order` liên quan $\rightarrow$ Tổng cộng có $1 + N$ câu SQL truy vấn.

#### 4 Giải pháp xử lý N+1 Query:
1. **`JOIN FETCH` trong JPQL / HQL**:
   ```java
   @Query("SELECT c FROM Customer c JOIN FETCH c.orders")
   List<Customer> findAllWithOrders();
   ```
2. **`@EntityGraph`**:
   ```java
   @EntityGraph(attributePaths = {"orders"})
   List<Customer> findAll();
   ```
3. **`@BatchSize`**: Gom nhóm các truy vấn con thành câu lệnh `IN (...)`.
   ```java
   @OneToMany(mappedBy = "customer")
   @BatchSize(size = 20)
   private List<Order> orders;
   ```
4. **DTO Projection**: Chỉ SELECT chính xác những column cần thiết thay vì load cả Entity Graph.

---

### 3.2 Quản lý Giao dịch với `@Transactional`
- **Cơ chế**: Spring dùng **AOP (Aspect Oriented Programming)** tạo Proxy bao bọc quanh Class/Method.
- **Propagation Types**:
  - `REQUIRED` (Default): Nếu đã có transaction thì tham gia vào, nếu chưa có thì tạo mới.
  - `REQUIRES_NEW`: Luôn tạo 1 transaction mới độc lập, suspend transaction hiện tại nếu có. (Rất hay dùng trong ghi Log giao dịch ngân hàng).
  - `MANDATORY`: Bắt buộc phải có transaction từ trước, nếu không sẽ ném Exception.
- **Lưu ý khi `@Transactional` KHÔNG hoạt động (Common Pitfalls)**:
  1. **Self-invocation**: Gọi internal method trong cùng 1 Class (Proxy không bắt được call này).
  2. Method không để access modifier là `public`.
  3. Quên cấu hình `rollbackFor = Exception.class` (Mặc định Spring chỉ rollback đối với `Unchecked Exception` / `RuntimeException`).

---

## PHẦN 4: RESTFUL API & KIẾN TRÚC MICROSERVICES

### 4.1 Thiết kế RESTful API Chuẩn Ngân Hàng
- **Nguồn tài nguyên (Resources)**: Sử dụng Danh từ số nhiều (`/api/v1/accounts`, `/api/v1/transactions`).
- **HTTP Methods & Idempotency**:
  - `GET`: Read-only $\rightarrow$ Idempotent.
  - `POST`: Create resource $\rightarrow$ **NON-Idempotent**.
  - `PUT`: Replace entire resource $\rightarrow$ Idempotent.
  - `PATCH`: Update partial fields $\rightarrow$ Có thể Idempotent hoặc không.
  - `DELETE`: Remove resource $\rightarrow$ Idempotent.

---

### 4.2 Pattern Microservices Phổ Biến
1. **API Gateway (Spring Cloud Gateway)**: Single entrypoint, thực hiện Routing, Rate Limiting, Authentication, Cross-Cutting Concerns.
2. **Service Discovery (Eureka / Consul)**: Quản lý IP/Port động của các microservice instance.
3. **Circuit Breaker (Resilience4j)**: Ngăn chặn lỗi dây chuyền (Cascading Failure).
   - Có 3 trạng thái: `CLOSED` (Bình thường) $\rightarrow$ `OPEN` (Ngắt kết nối, trả về Fallback ngay lập tức) $\rightarrow$ `HALF-OPEN` (Thử nghiệm gọi lại một số request).
4. **Centralized Configuration**: Spring Cloud Config Server / Vault lưu trữ config tập trung và mã hóa secret key.

---

## PHẦN 5: QUẢN TRỊ CSDL (ORACLE & MYSQL)

### 5.1 Tối ưu hóa Index (Oracle & MySQL)
- **B-Tree Index**: Cấu trúc dữ liệu mặc định của Index.
- **Clustered Index vs Non-Clustered Index**:
  - **Clustered Index (MySQL InnoDB)**: Dữ liệu thực tế được sắp xếp trực tiếp theo Primary Key trên B-Tree. Mỗi bảng chỉ có 1 Clustered Index.
  - **Non-Clustered Index (Secondary Index)**: Lưu giá trị Index và con trỏ trỏ về Primary Key (hoặc ROWID trong Oracle).
- **Khi nào NÊN và KHÔNG NÊN đánh Index?**
  - **NÊN**: Các cột xuất hiện thường xuyên trong `WHERE`, `JOIN`, `ORDER BY`, `GROUP BY`, cột có độ đa dạng dữ liệu cao (High Cardinality - VD: Số tài khoản, CMND/CCCD).
  - **KHÔNG NÊN**: Cột có Low Cardinality (VD: Giới tính `M/F`, Trạng thái `0/1`), các bảng ghi/xóa liên tục với số lượng đọc ít (tốn chi phí re-index).

---

### 5.2 Optimistic Locking vs Pessimistic Locking
Trong bài toán **Chuyển tiền / Trừ tiền tài khoản (Race Condition)**:

#### 1. Optimistic Locking (Khoá quan sát / Lạc quan)
- Sử dụng cột `@Version` trong JPA Entity.
- Không khóa dòng dưới DB. Khi commit, Hibernate kiểm tra version. Nếu version bị thay đổi bởi giao dịch khác $\rightarrow$ Thăng cấp lỗi `OptimisticLockException`.
- **Phù hợp**: Hệ thống có tỷ lệ xung đột đọc/ghi thấp.

#### 2. Pessimistic Locking (Khoá bi quan)
- Sử dụng câu lệnh `SELECT ... FOR UPDATE`.
- Khóa trực tiếp record dưới DB cho đến khi transaction hoàn tất.
- **Phù hợp**: Giao dịch tài chính nhạy cảm, độ xung đột ghi cao.

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT a FROM Account a WHERE a.id = :id")
Optional<Account> findByIdForUpdate(@Param("id") Long id);
```

---

## PHẦN 6: BANKING DOMAIN & KỸ THUẬT NÂNG CAO

### 6.1 Thiết Kế API Chuyển Tiền An Toàn (Idempotent API)
**Bài toán**: Client bấm nút "Chuyển tiền" 2 lần do mạng lag hoặc timeout. Làm sao đảm bảo tài khoản không bị trừ tiền 2 lần?

**Giải pháp với Request Header `X-Idempotency-Key`**:
1. Client sinh ra 1 UUID duy nhất cho mỗi giao dịch và gửi kèm qua HTTP Header `X-Idempotency-Key`.
2. API Gateway hoặc Service nhận request, kiểm tra `Idempotency-Key` trong Redis (dùng Redis Atomic `SETNX` với TTL).
3. Nếu Key đã tồn tại:
   - Trạng thái `PROCESSING`: Trả về lỗi `409 Conflict` hoặc yêu cầu chờ.
   - Trạng thái `COMPLETED`: Trả về ngay kết quả đã lưu trong Redis mà không thực hiện lại logic trừ tiền.
4. Nếu Key chưa tồn tại: Thực hiện giao dịch DB trong `@Transactional` $\rightarrow$ Lưu kết quả vào Redis $\rightarrow$ Trả về `200 OK`.

---

### 6.2 Distributed Transactions: Saga Pattern vs 2PC
Trong Microservices, mỗi service có DB riêng, không thể dùng `@Transactional` thông thường.

#### 1. 2PC (Two-Phase Commit)
- Cần Transaction Coordinator. Phân làm 2 phase: Prepare & Commit.
- **Nhược điểm**: Khóa tài nguyên lâu, giảm throughput, không phù hợp cho Microservices hiện đại.

#### 2. Saga Pattern (Khuyên dùng)
- Chuỗi các local transaction. Mỗi local transaction cập nhật DB và phát sự kiện (Event) để kích hoạt transaction tiếp theo.
- **Nếu 1 step thất bại**: Hệ thống sẽ phát các **Compensating Transactions** (Giao dịch bù trừ) để khôi phục lại dữ liệu cũ.
- **2 loại Saga**:
  - **Choreography Saga**: Các service tự lắng nghe Event của nhau qua Kafka/RabbitMQ (Phù hợp workflow đơn giản).
  - **Orchestration Saga**: Có 1 `Saga Orchestrator` điều phối tập trung (Phù hợp workflow ngân hàng phức tạp).

---

## PHẦN 7: TỔNG HỢP 30+ CÂU HỎI PHỎNG VẤN TRỰC TIẾP & CÂU TRẢ LỜI MẪU

### Q1: Sự khác biệt giữa Interface và Abstract Class từ Java 8 trở đi là gì?
> **Trả lời mẫu**: 
> Từ Java 8, Interface có thể chứa `default` method và `static` method có body. Từ Java 9 có thêm `private` method.
> Tuy nhiên, điểm khác biệt bản chất là:
> - **Abstract class** có thể giữ trạng thái (state) với các instance variables (fields) non-final. Một class chỉ có thể `extends` 1 Abstract Class. Thể hiện mối quan hệ "IS-A".
> - **Interface** không có instance fields (mọi field đều là `public static final`). Một class có thể `implements` nhiều Interface. Thể hiện mối quan hệ "CAN-DO".

### Q2: Em hãy giải thích cơ chế Garbage Collection (GC) trong Java?
> **Trả lời mẫu**:
> GC quản lý bộ nhớ Heap tự động bằng cách tìm và hủy các Object không còn "Reachable" từ GC Roots (như local variables trên Thread Stack, static fields).
> Bộ nhớ Heap chia thành:
> - **Young Generation**: Chứa object mới tạo (Eden, Survivor S0, S1). Dùng **Minor GC** (tần suất cao, chạy nhanh).
> - **Old Generation**: Chứa object sống sót qua nhiều lần Minor GC. Dùng **Major GC / Full GC** (tốn nhiều thời gian, có thể gây lỗi Stop-The-World).

### Q3: Tác hại của lỗi N+1 Query và làm sao phát hiện nó trong log?
> **Trả lời mẫu**:
> N+1 Query làm bùng nổ số lượng truy vấn DB, gây cạn kiệt DB Connection Pool và tăng thời gian phản hồi API.
> **Cách phát hiện**: 
> 1. Bật log SQL của Spring: `spring.jpa.show-sql=true` hoặc dùng thư viện `datasource-proxy`.
> 2. Đọc log thấy 1 câu SQL fetch danh sách cha và ngay sau đó là vô số câu SQL dạng `SELECT * FROM child WHERE parent_id = ?`.

### Q4: Sự khác biệt giữa `@Component`, `@Service`, `@Repository` là gì?
> **Trả lời mẫu**:
> Về bản chất, `@Service` và `@Repository` đều là meta-annotation kế thừa từ `@Component`, nên Spring IoC đều coi chúng là Bean.
> Tuy nhiên có sự khác biệt về mặt ngữ nghĩa và tính năng:
> - `@Component`: Annotation tổng quát cho bất kỳ Spring Bean nào.
> - `@Service`: Đánh dấu lớp chứa Business Logic.
> - `@Repository`: Đánh dấu lớp truy xuất dữ liệu (DAO). Có tính năng tự động catch các SQLException của vendor và chuyển đổi (translate) thành `DataAccessException` của Spring.

### Q5: Khi nào sử dụng Oracle Database thay vì MySQL trong các bài toán Ngân hàng?
> **Trả lời mẫu**:
> - **Oracle Database** nổi tiếng với khả năng chịu tải cực cao (Enterprise scale), độ an toàn dữ liệu, công nghệ Oracle RAC (Real Application Clusters), hỗ trợ Partitioning và PL/SQL cực mạnh cho các nghiệp vụ Core Banking, đối soát tài chính lớn.
> - **MySQL (hoặc PostgreSQL)** thường được ưu tiên cho các dịch vụ Microservices vệ tinh, các hệ thống ứng dụng phụ trợ nhờ tính linh hoạt, chi phí bản quyền tối ưu và dễ dàng scale-out.
