# Java 8+ Functional Programming, Lambda Expressions, Stream API & Optional

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Lập trình chức năng (Functional Programming), Stream API và Optional** trong Java 8+ dành cho lập trình viên Java 2 năm kinh nghiệm chuẩn bị phỏng vấn và thi chứng chỉ OCP.

---

## Phần 1: Functional Interfaces & Lambda Expressions

### 1. Functional Interface là gì?
* **Định nghĩa:** Là một Interface **chỉ chứa duy nhất một phương thức trừu tượng (Single Abstract Method - SAM)**.
* Annotation `@FunctionalInterface`: Dùng để thông báo cho trình biên dịch kiểm tra tính hợp lệ. Nếu Interface chứa nhiều hơn hoặc ít hơn 1 abstract method, Compiler sẽ báo lỗi.
* **Lưu ý OCP:** Functional Interface có thể chứa nhiều `default methods`, `static methods` và các phương thức `public` của `Object` (`toString()`, `equals()`), nhưng **chỉ đúng 1 abstract method duy nhất**.

---

### 2. Bộ 4 Core Functional Interfaces (Phải thuộc lòng)

| Functional Interface | Phương thức trừu tượng | Đầu vào | Đầu ra | Ứng dụng thực tế |
| :--- | :--- | :--- | :--- | :--- |
| **`Supplier<T>`** | `T get()` | Không | `T` | Sinh/cung cấp dữ liệu (ví dụ: tạo object, lấy config). |
| **`Consumer<T>`** | `void accept(T t)` | `T` | `void` | Tiêu thụ/xử lý dữ liệu (ví dụ: `System.out.println`, ghi DB). |
| **`Predicate<T>`** | `boolean test(T t)` | `T` | `boolean` | Kiểm tra điều kiện/lọc dữ liệu (`filter()`). |
| **`Function<T, R>`** | `R apply(T t)` | `T` | `R` | Biến đổi dữ liệu từ kiểu `T` sang kiểu `R` (`map()`). |

#### Các biến thể chuyên biệt (Specialized Variations):
* **Bi-variants:** `BiFunction<T, U, R>`, `BiConsumer<T, U>`, `BiPredicate<T, U>` (Nhận vào 2 tham số).
* **Operators:** 
  * `UnaryOperator<T>` (Kế thừa `Function<T, T>`): Nhận vào `T` trả về cùng kiểu `T`.
  * `BinaryOperator<T>` (Kế thừa `BiFunction<T, T, T>`): Nhận 2 tham số cùng kiểu `T` trả về `T` (dùng trong `reduce`).
* **Primitive Specializations (Tối ưu hiệu năng):** `IntPredicate`, `LongConsumer`, `DoubleFunction`... Giúp tránh chi phí **Autoboxing/Unboxing** giữa primitive và wrapper class.

---

### 3. Quy tắc "Effectively Final" Trong Lambda

Một câu hỏi phỏng vấn cực kỳ phổ biến: **"Tại sao biến cục bộ (Local Variable) khi dùng trong Lambda Expression phải là `final` hoặc `effectively final`?"**

```java
int count = 10;
// count = 20; // ❌ Nếu bỏ comment dòng này, Lambda bên dưới sẽ bị LỖI BIÊN DỊCH!
Runnable r = () -> System.out.println(count);
```

#### Giải thích nguyên nhân từ JVM:
1. **Khác biệt bộ nhớ:** Biến cục bộ nằm trên **Stack Frame** của phương thức. Biến trong Lambda Expression có thể chạy trên một Luồng (Thread) khác sống lâu hơn thời gian tồn tại của phương thức đó.
2. **Cơ chế Variable Capture (Bắt giữ biến):** Khi tạo Lambda, Java không dùng trực tiếp biến cục bộ đó mà **tạo một bản sao (copy)** của biến đó vào trong Lambda.
3. Để tránh tình trạng lệch dữ liệu giữa bản sao trong Lambda và bản gốc trên Stack, Java bắt buộc biến đó không được thay đổi giá trị (`effectively final`).

---

### 4. Method References (Tham chiếu phương thức `::`)

Có 4 loại Method Reference chính:

| Loại | Cú pháp | Ví dụ Lambda tương đương |
| :--- | :--- | :--- |
| **Static Method** | `ClassName::staticMethod` | `str -> Integer.parseInt(str)` $\rightarrow$ `Integer::parseInt` |
| **Instance Method của đối tượng cụ thể** | `instance::instanceMethod` | `x -> System.out.println(x)` $\rightarrow$ `System.out.println::println` |
| **Instance Method của đối tượng tùy định** | `ClassName::instanceMethod` | `(str) -> str.toLowerCase()` $\rightarrow$ `String::toLowerCase` |
| **Constructor** | `ClassName::new` | `() -> new ArrayList<>()` $\rightarrow$ `ArrayList::new` |

---

## Phần 2: Stream API Deep-Dive

Stream API KHÔNG phải là một cấu trúc dữ liệu để lưu trữ phần tử. Stream là một **dòng chảy dữ liệu (Data Pipeline)** cho phép xử lý dữ liệu theo phong cách khai báo (Declarative).

```
Source (List/Set/Array) ──> Intermediate Operations (filter/map) ──> Terminal Operation (collect/count)
```

---

### 1. Cơ chế Lazy Evaluation (Đánh giá lười biếng)

Đây là trái tim về mặt hiệu năng của Stream API!

* **Đặc điểm:** Các thao tác trung gian (**Intermediate Operations**) như `filter()`, `map()`, `sorted()` **KHÔNG BAO GIỜ THỰC THI NGAY** khi được gọi.
* Chúng chỉ xây dựng một kế hoạch xử lý (Execution Plan).
* Toàn bộ pipeline chỉ thực sự chạy khi một thao tác kết thúc (**Terminal Operation**) như `collect()`, `count()`, `findFirst()` được kích hoạt.

#### Ví dụ chứng minh:
```java
List<String> names = Arrays.asList("Alice", "Bob", "Charlie");

Stream<String> stream = names.stream().filter(name -> {
    System.out.println("Filtering: " + name); // ❌ Dòng này CHƯA CHẠY!
    return name.startsWith("A");
});

System.out.println("--- Trước khi gọi Terminal Operation ---");
stream.collect(Collectors.toList()); // 🔥 Lúc này "Filtering: ..." mới bắt đầu in ra!
```

---

### 2. Phân biệt `map()` và `flatMap()` (Câu hỏi phỏng vấn kinh điển)

| Tiêu chí | `map()` | `flatMap()` |
| :--- | :--- | :--- |
| **Chức năng** | Biến đổi từng phần tử $1 \rightarrow 1$. | Phẳng hóa (flatten) cấu trúc lồng nhau $1 \rightarrow N$. |
| **Đầu vào của hàm** | `Function<T, R>` | `Function<T, Stream<R>>` |
| **Kiểu trả về** | `Stream<R>` | `Stream<R>` (đã được làm phẳng) |
| **Trường hợp sử dụng** | Đổi `Employee` thành `EmployeeDTO`, chuyển chuỗi sang chữ hoa. | Biến `List<List<String>>` hoặc `Stream<Orders>` (chứa list Item) thành một `Stream<Item>` duy nhất. |

```java
// Ví dụ flatMap: Phẳng hóa List các List thành 1 List duy nhất
List<List<String>> nestedList = Arrays.asList(
    Arrays.asList("A", "B"),
    Arrays.asList("C", "D")
);

List<String> flatList = nestedList.stream()
    .flatMap(Collection::stream) // Chuyển từng List nhỏ thành Stream rồi gộp lại
    .collect(Collectors.toList()); // Result: ["A", "B", "C", "D"]
```

---

### 3. Phân Nhóm Nâng Cao Với `Collectors.groupingBy`

Trong thực tế dự án, `groupingBy` được sử dụng cực kỳ nhiều để nhóm dữ liệu tương tự câu lệnh `GROUP BY` trong SQL.

```java
List<Employee> employees = getEmployees();

// 1. Nhóm nhân viên theo Phòng Ban (Department)
Map<Department, List<Employee>> byDept = employees.stream()
    .collect(Collectors.groupingBy(Employee::getDepartment));

// 2. Nhóm nhân viên theo Phòng Ban và tính Tổng Lương mỗi phòng
Map<Department, Double> totalSalaryByDept = employees.stream()
    .collect(Collectors.groupingBy(
        Employee::getDepartment,
        Collectors.summingDouble(Employee::getSalary)
    ));
```

---

### 4. Parallel Stream & Cạm Bẫy ForkJoinPool

* `parallelStream()` chia nhỏ dữ liệu bằng `Spliterator` và xử lý đa luồng thông qua **`ForkJoinPool.commonPool()`** dùng chung toàn bộ ứng dụng JVM.

#### ⚠️ Cạm bẫy chết người khi dùng Parallel Stream:
1. **Nghẽn toàn hệ thống (Thread Starvation):** Nếu trong Parallel Stream em gọi các tác vụ I/O chậm (gọi API ngoài, truy vấn DB), các thread trong `commonPool` sẽ bị block $\rightarrow$ Khiến tất cả các Parallel Stream khác trong toàn ứng dụng bị "tê liệt"!
2. **Race Condition / State Mutation:** Tuyệt đối không sửa đổi biến bên ngoài (Shared State) trong `forEach` của Parallel Stream.
3. **Không phải lúc nào cũng nhanh hơn:** Với tập dữ liệu nhỏ hoặc cấu trúc khó chia nhỏ (`LinkedList`), chi phí phân tách luồng và gộp kết quả của `Parallel Stream` sẽ làm chương trình chạy **chậm hơn** hẳn so với Stream tuần tự.

---

## Phần 3: `Optional<T>` Best Practices

`Optional` được giới thiệu để giải quyết vấn đề **`NullPointerException` (NPE)** - "Lỗi một tỷ đô la".

---

### 1. Các Quy Tắc Sử Dụng `Optional` Chuẩn Senior

1. **NÊN dùng:** Làm kiểu trả về (Return Type) cho phương thức khi kết quả có thể không tìm thấy (ví dụ: `findByUserId(id)`).
2. **KHÔNG NÊN dùng:**
   * Không làm thuộc tính (Field) của Class (vì `Optional` không implement `Serializable`).
   * Không dùng làm tham số truyền vào của phương thức (Method Parameter).
   * Không dùng chứa phần tử trong Collection (dùng `List` rỗng thay vì `Optional<List>`).
3. **KHÔNG BAO GIỜ gọi `.get()` trực tiếp** mà không kiểm tra `isPresent()`.

---

### 2. Phân biệt `orElse()` và `orElseGet()` (Cực hay hỏi phỏng vấn)

```java
public String getDefault() {
    System.out.println("Đang tạo giá trị mặc định..."); // Tác vụ nặng
    return "Default";
}

Optional<String> opt = Optional.of("Alice");

// Cách 1: orElse
String res1 = opt.orElse(getDefault()); 
// ⚠️ In ra: "Đang tạo giá trị mặc định..." (VẪN CHẠY hàm dù opt KHÔNG RỖNG!)

// Cách 2: orElseGet
String res2 = opt.orElseGet(() -> getDefault()); 
//  KHÔNG IN RA GÌ CẢ (Chỉ chạy Supplier khi opt RỖNG!)
```

* **Kết luận:** Always prefer **`orElseGet()`** khi giá trị mặc định phải tính toán qua phương thức/tác vụ tốn chi phí.

---

## 📌 Summary Sheet Chặng 2

1. **Core Functional Interfaces:** `Supplier<T>` (`get`), `Consumer<T>` (`accept`), `Predicate<T>` (`test`), `Function<T,R>` (`apply`).
2. **Effectively Final:** Biến cục bộ trong Lambda phải không đổi do cơ chế *Variable Capture* trên Stack.
3. **Lazy Evaluation:** Intermediate operations (`filter`, `map`) chỉ chạy khi gọi Terminal operation (`collect`, `count`).
4. **`map` vs `flatMap`:** `map` biến đổi $1 \rightarrow 1$; `flatMap` làm phẳng cấu trúc lồng nhau $1 \rightarrow N$.
5. **Parallel Stream:** Dùng chung `ForkJoinPool.commonPool()`, cấm gọi I/O block hoặc mutable state.
6. **`Optional`:** Tránh `.get()`, ưu tiên `orElseGet()` thay vì `orElse()`.

---
*Tài liệu này tổng hợp toàn bộ trọng tâm Java 8+ Functional Programming!*
