# Hợp Đồng equals() & hashCode() và Cơ Chế Bên Trong của HashMap

Chào em! Đây là tài liệu chuyên sâu về **Collections Framework & Hash Mechanism** dành cho lập trình viên Java có kinh nghiệm. Đây là một trong những chủ đề xuất hiện với tần suất 99% trong các buổi phỏng vấn vị trí Java Developer (Middle/Senior) và các bài thi chứng chỉ OCP.

---

## Phần 1: Hợp Đồng (Contract) Giữa `equals()` và `hashCode()`

### 1. Tại sao Java cần cả hai phương thức này?
* **`equals(Object obj)`**: Dùng để so sánh **tính bằng nhau về mặt logic (Logical Equality)** giữa hai đối tượng. Phép so sánh này chính xác tuyệt đối nhưng có thể chậm nếu đối tượng phức tạp ($O(N)$).
* **`hashCode()`**: Trả về một số nguyên (`int`) đại diện cho vị trí băm của đối tượng. Phép tính này cực kỳ nhanh ($O(1)$) để định vị nhanh đối tượng trong các cấu trúc băm (Hash-based collections như `HashMap`, `HashSet`, `Hashtable`).

---

### 2. Ba Quy Tắc Vàng Của Hợp Đồng (The Contract Rules)

Khi em override phương thức `equals()`, em **BẮT BUỘC** phải tuân thủ 3 quy tắc sau:

1. **Quy tắc 1 (Bắt buộc):** Nếu `a.equals(b) == true` thì chắc chắn `a.hashCode() == b.hashCode()`.
2. **Quy tắc 2:** Nếu `a.hashCode() == b.hashCode()` thì `a.equals(b)` **CHƯA CHẮC** đã bằng `true` (Xảy ra hiện tượng đụng độ băm - **Hash Collision**).
3. **Quy tắc 3:** Nếu `equals()` không thay đổi (các thuộc tính dùng để so sánh giữ nguyên), thì việc gọi `hashCode()` nhiều lần trên cùng một đối tượng phải luôn trả về cùng một số nguyên.

---

### 3. Thảm Họa Khi Override `equals()` Mà KHÔNG Override `hashCode()`

Đây là câu hỏi phỏng vấn kinh điển: **"Chuyện gì xảy ra nếu tôi chỉ override `equals()` mà quên override `hashCode()`?"**

#### Ví dụ thực tế:
```java
class Student {
    private int id;
    private String name;

    public Student(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Chỉ override equals()
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return id == student.id && Objects.equals(name, student.name);
    }

    // ❌ KHÔNG OVERRIDE hashCode() -> Sử dụng hashCode() mặc định của Object (dựa trên địa chỉ bộ nhớ)
}
```

#### Hậu quả khi dùng với `HashMap`/`HashSet`:
```java
Map<Student, String> map = new HashMap<>();
Student s1 = new Student(1, "Alice");
Student s2 = new Student(1, "Alice");

// s1.equals(s2) là TRUE về mặt logic!
map.put(s1, "PASSED");

System.out.println(map.get(s2)); // ❌ KẾT QUẢ: null !!!
```
* **Giải thích nguyên nhân:**
  1. Khi gọi `map.put(s1, "PASSED")`, `HashMap` dựa vào `s1.hashCode()` mặc định (địa chỉ bộ nhớ) để tính vị trí thùng (Bucket). Giả sử rơi vào Bucket #3.
  2. Khi gọi `map.get(s2)`, vì `s2` là đối tượng mới nên `s2.hashCode()` ngầm định sẽ ra một con số hoàn toàn khác với `s1`. `HashMap` tìm `s2` ở Bucket #8.
  3. Bucket #8 đang rỗng $\rightarrow$ `HashMap` trả về `null` ngay lập tức mà **không bao giờ gọi tới hàm `equals()`** của em! Dữ liệu bị thất lạc.

---

### 4. Cạm bẫy Mutable Key trong `HashMap`

Một bẫy phỏng vấn cực kỳ phổ biến: **"Nếu dùng một đối tượng có thể thay đổi (Mutable Object) làm Key trong `HashMap`, điều gì sẽ xảy ra?"**

```java
Student s = new Student(1, "Bob"); // Giả sử Student có setter
map.put(s, "Grade A");

// Thay đổi thuộc tính của s sau khi đã đưa vào Map
s.setName("Robert"); 

// Bây giờ hashCode của s đã bị thay đổi!
System.out.println(map.get(s)); // ❌ KẾT QUẢ: null
```
* **Bài học rút ra:** Key trong `HashMap` nên là các **Immutable Objects** (như `String`, `Integer`, `UUID`, hoặc `Record` trong Java 14+) để `hashCode` không bao giờ bị biến đổi sau khi đã cho vào Map.

---

## Phần 2: Cơ Chế Vận Hành Bên Trong Của `HashMap` (Java 8+)

### 1. Cấu trúc dữ liệu nền tảng
Bên trong `HashMap` là một mảng của các Node (gọi là mảng Buckets):
```java
transient Node<K,V>[] table;
```
Mỗi phần tử `Node<K,V>` chứa:
* `final int hash`: Giá trị hash code đã qua xử lý.
* `final K key`: Khóa.
* `V value`: Giá trị.
* `Node<K,V> next`: Con trỏ trỏ tới Node tiếp theo (dạng LinkedList).

```
Mảng table (Buckets):
Index 0: null
Index 1: [Node A] -> [Node B] -> null  (LinkedList - Collision)
Index 2: null
Index 3: [TreeNode R] (Red-Black Tree - Cây đỏ đen khi bucket >= 8)
```

---

### 2. Thuật toán `put(K key, V value)` từng bước

Khi em gọi `map.put(key, value)`, JVM thực hiện các bước sau:

#### Bước 1: Tính toán Hash Code (Hash Spreading)
```java
static final int hash(Object key) {
    int h;
    return (key == null) ? 0 : (h = key.hashCode()) ^ (h >>> 16);
}
```
* Phép dịch bít `h >>> 16` và XOR (`^`) giúp đưa các bít cao xuống bít thấp, giúp làm đều giá trị hash và giảm thiểu tối đa đụng độ băm.

#### Bước 2: Tính vị trí Index trong mảng
$$\text{Index} = (n - 1) \ \& \ \text{hash}$$
(Trong đó $n$ là độ dài mảng Buckets).
* **Tại sao mảng `HashMap` luôn có dung lượng là lũy thừa của 2 ($16, 32, 64, \dots$)?**
  * Vì khi $n = 2^k$, toán tử bít `(n - 1) & hash` có kết quả **hoàn toàn tương đương với phép chia lấy dư `hash % n`**, nhưng tốc độ xử lý ở cấp độ CPU bằng toán tử `&` nhanh hơn phép chia `%` gấp nhiều lần!

#### Bước 3: Xử lý chèn vào Bucket
1. Nếu `table[index] == null`: Tạo Node mới và đặt trực tiếp vào Index đó.
2. Nếu xảy ra đụng độ (`table[index] != null`):
   * Duyệt qua danh sách (LinkedList hoặc Tree):
   * Nếu thấy Key đã tồn tại (`p.hash == hash && (p.key == key || key.equals(p.key))`): **Ghi đè (overwrite)** value cũ bằng value mới.
   * Nếu Key chưa tồn tại: Thêm Node mới vào **cuối** danh sách (Java 8+ chèn vào đuôi - Tail Insertion, tránh lỗi lặp vô hạn như Java 7 Head Insertion).

#### Bước 4: Cây hóa (Treeification) - Bước cải tiến Java 8
* Khi số lượng Node trong **cùng 1 Bucket** đạt $\ge 8$ (`TREEIFY_THRESHOLD = 8`) **VÀ** tổng dung lượng mảng `table` đạt $\ge 64$ (`MIN_TREEIFY_CAPACITY = 64`):
  * `HashMap` sẽ chuyển đổi LinkedList của Bucket đó thành một **Red-Black Tree (Cây đỏ - đen)** (`TreeNode`).
  * **Tác dụng:** Giảm độ phức tạp tìm kiếm từ $O(N)$ (LinkedList) xuống còn **$O(\log N)$** (Red-Black Tree), chống lại các cuộc tấn công từ chối dịch vụ DOS bằng Hash Collision!
* Nếu số lượng phần tử trong Bucket giảm xuống $\le 6$ (`UNTREEIFY_THRESHOLD = 6`), nó sẽ tự động chuyển ngược từ Red-Black Tree về lại LinkedList.

#### Bước 5: Tăng kích thước (Resize / Rehashing)
* Nếu số lượng phần tử (`size`) vượt quá ngưỡng `threshold` ($\text{threshold} = \text{capacity} \times \text{loadFactor}$, mặc định $16 \times 0.75 = 12$):
  * Mảng `table` sẽ nhân đôi kích thước ($16 \rightarrow 32 \rightarrow 64 \dots$).
  * Tất cả phần tử sẽ được phân bổ lại vị trí mới (Rehash). Trong Java 8, một phần tử sẽ giữ nguyên index cũ hoặc chuyển sang index mới bằng `index_mới = index_cũ + dung_lượng_cũ`.

---

## Phần 3: Bản chất bên trong của `HashSet`

Rất nhiều bạn ngạc nhiên khi biết rằng `HashSet` **không hề có thuật toán băm riêng**!

Bên trong `HashSet`, nó chỉ là một lớp bọc (**Wrapper**) cho `HashMap`:
```java
public class HashSet<E> {
    private transient HashMap<E, Object> map;
    private static final Object PRESENT = new Object(); // Dummy object

    public HashSet() {
        map = new HashMap<>();
    }

    public boolean add(E e) {
        return map.put(e, PRESENT) == null; // Dùng Element làm Key, Value là dummy Object
    }
}
```
* **Kết luận:** Mọi đặc tính của `HashSet` (không trùng lặp, thứ tự không cố định, hiệu năng $O(1)$) đều kế thừa trực tiếp từ cơ chế của `HashMap`.

---

## Phần 4: So Sánh ConcurrentHashMap vs Hashtable vs Collections.synchronizedMap()

Khi ứng dụng chạy Đa luồng (Multithreading), việc chọn đúng loại Map quyết định sống còn đến hiệu năng ứng dụng.

| Tiêu chí | `Hashtable` | `Collections.synchronizedMap()` | `ConcurrentHashMap` (Java 8+) |
| :--- | :--- | :--- | :--- |
| **Cơ chế Khóa (Locking)** | Khóa **toàn bộ** phương thức bằng `synchronized`. | Khóa **toàn bộ** Map thông qua một đối tượng mutex. | **Không khóa toàn bộ!** Dùng **CAS** cho bucket rỗng + **`synchronized` trên từng Bucket Head Node** khi chèn. |
| **Hiệu năng Đa luồng** | Cực kỳ chậm (Nghẽn cổ chai). | Chậm (Nghẽn cổ chai khi nhiều luồng). | **Cực kỳ cao** (Các luồng đọc/ghi ở các bucket khác nhau chạy song song 100%). |
| **Thao tác Đọc (`get`)** | Bị khóa (`synchronized`). | Bị khóa (`synchronized`). | **Không bị khóa!** (Dùng `volatile` để đọc dữ liệu mới nhất mà không tốn chi phí lock). |
| **Khóa/Giá trị `null`** | ❌ Cấm `null` key & `null` value. |  Cho phép `null` key & `null` value. | ❌ **Cấm tuyệt đối** `null` key & `null` value. |

### Tại sao `ConcurrentHashMap` lại cấm `null` key và `null` value?
* Trong môi trường đơn luồng (`HashMap`), nếu `map.get("key")` trả về `null`, em có thể dùng `map.containsKey("key")` để biết là "Key không tồn tại" hay "Key có tồn tại nhưng value bằng null".
* Trong môi trường đa luồng (`ConcurrentHashMap`), giữa thời điểm em gọi `get()` và `containsKey()`, một thread khác có thể đã sửa/xóa key đó. Sự mập mờ của `null` sẽ gây ra bug sai lệch luồng dữ liệu (Race Condition). Do đó, tác giả Doug Lea đã quyết định **cấm hoàn toàn `null`**.

---

## 🎯 Cheat Sheet Các Câu Hỏi Phỏng Vấn "Ăn Điểm" 2 YOE

1. **Hỏi:** *Tại sao `loadFactor` mặc định lại là `0.75`?*
   * **Trả lời:** Đây là con số đánh đổi tối ưu nhất giữa thời gian (Time Complexity - ít trùng lặp hash) và bộ nhớ (Space Complexity). Nếu là `1.0`, tiết kiệm bộ nhớ nhưng xung đột hash tăng cao. Nếu là `0.5`, ít xung đột nhưng lãng phí $50\%$ bộ nhớ mảng.

2. **Hỏi:** *Độ phức tạp thời gian (Time Complexity) của `HashMap.get()` là bao nhiêu?*
   * **Trả lời:** 
     * Trường hợp trung bình: **$O(1)$**.
     * Trường hợp xấu nhất (Tất cả key cùng đụng độ hash): **$O(\log N)$** trong Java 8+ (nhờ Red-Black Tree) thay vì $O(N)$ như Java 7 trở về trước.

3. **Hỏi:** *Điểm khác biệt chính trong thuật toán `put` của Java 7 và Java 8 là gì?*
   * **Trả lời:** 
     * Java 7: Chèn vào đầu LinkedList (**Head Insertion**), có nguy cơ bị vòng lặp vô hạn (Infinite Loop) khi `resize` đa luồng. Chỉ dùng LinkedList ($O(N)$).
     * Java 8: Chèn vào đuôi (**Tail Insertion**), giải quyết lỗi vòng lặp vô hạn. Chuyển LinkedList sang **Red-Black Tree** ($O(\log N)$) khi bucket $\ge 8$.

---

## 📌 Tóm Tắt Nhanh (Summary Sheet)

1. **Hợp đồng `equals()` & `hashCode()`:** 
   * `a.equals(b) == true` $\Rightarrow$ `a.hashCode() == b.hashCode()` (Bắt buộc).
   * Quên override `hashCode()` $\Rightarrow$ `HashMap.get()` trả về `null` dù key bằng nhau về mặt logic.
   * `HashMap` key nên là **Immutable Object** (`String`, `Integer`).

2. **Cơ chế vận hành `HashMap` (Java 8+):**
   * Mảng Bucket `Node<K,V>[] table`.
   * Vị trí Index: `(n - 1) & hash` (nhanh gấp nhiều lần phép chia `%`, yêu cầu dung lượng $n = 2^k$).
   * Xử lý xung đột hash: Chèn đuôi (Tail Insertion). Cây hóa (**Red-Black Tree**) khi 1 Bucket $\ge 8$ phần tử và tổng capacity $\ge 64$, giảm tìm kiếm từ $O(N)$ xuống **$O(\log N)$**.
   * Resize khi `size > capacity * loadFactor` ($16 \times 0.75 = 12$).

3. **Bản chất `HashSet`:**
   * Chỉ là một **Wrapper xung quanh `HashMap`**, lưu phần tử làm Key và Value là một dummy `Object`.

4. **`ConcurrentHashMap` (Java 8+):**
   * Không khóa toàn bộ Map. Dùng **CAS** cho bucket rỗng + **`synchronized` trên Bucket Head Node** khi chèn.
   * Thao tác `get()` không dùng lock (`volatile` read).
   * **Cấm `null` key và `null` value** để tránh mơ hồ luồng dữ liệu (Race Condition).

---
*Hãy đọc kỹ tài liệu này và thử viết code kiểm chứng các trường hợp trên nhé!*
