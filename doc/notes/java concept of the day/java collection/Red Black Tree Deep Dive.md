# Cây Đỏ - Đen (Red-Black Tree) Và Cơ Chế Hoạt Động Trong Java

Tài liệu này hệ thống hóa toàn bộ kiến thức chuyên sâu về **Cây Đỏ - Đen (Red-Black Tree)**, 5 quy tắc vàng, thuật toán tự cân bằng (Xoay & Đổi màu) và lý do Java chọn Cây Đỏ - Đen trong `TreeMap`, `TreeSet`, `HashMap`.

---

## 1. Cây Đỏ - Đen Là Gì?

**Cây Đỏ - Đen (Red-Black Tree)** là một cấu trúc dữ liệu **Cây tìm kiếm nhị phân tự cân bằng (Self-Balancing Binary Search Tree - BST)**.

```
                  ┌─────────┐
                  │ 13 (B)  │  <-- Node Gốc (Root - Đen)
                  └────┬────┘
           ┌───────────┴───────────┐
     ┌─────┴─────┐           ┌─────┴─────┐
     │  8 (R)    │           │  17 (R)   │
     └─────┬─────┘           └─────┬─────┘
   ┌───────┴───────┐       ┌───────┴───────┐
┌──┴──┐         ┌──┴──┐ ┌──┴──┐         ┌──┴──┐
│1(B) │         │11(B)│ │15(B)│         │25(B)│
└─────┘         └─────┘ └─────┘         └─────┘
```

### Tại sao lại cần "Tự Cân Bằng"?
* Trong một Cây tìm kiếm nhị phân (BST) thông thường, nếu bạn thêm các phần tử theo thứ tự tăng dần (`1, 2, 3, 4, 5`), cây sẽ bị **thoái hóa thành một Danh sách liên kết (LinkedList)** dải thẳng.
* Lúc này độ phức tạp tìm kiếm bị kéo lùi từ $O(\log N)$ thành **$O(N)$** (chạy rất chậm).
* **Cây Đỏ - Đen ra đời để khắc phục vấn đề này:** Thông qua các quy tắc gắn màu ĐỎ/ĐEN và thao tác Xoay cây, chiều cao của cây luôn được giữ ở mức $O(\log N)$, đảm bảo độ phức tạp cho các thao tác **Tìm kiếm (`search`), Thêm (`insert`), Xóa (`delete`) LUÔN LUÔN là $O(\log N)$**.

---

## 2. 5 Quy Tắc Vàng Cấu Thành Cây Đỏ - Đen (Invariants)

Mọi Cây Đỏ - Đen **BẮT BUỘC** phải luôn tuân thủ 5 quy tắc sau tại mọi thời điểm:

1. **Quy tắc 1 (Màu sắc):** Mỗi Node trên cây chỉ có thể là màu **ĐỎ (RED)** hoặc **ĐEN (BLACK)**.
2. **Quy tắc 2 (Node Gốc):** Node Gốc (Root) của cây luôn luôn là màu **ĐEN**.
3. **Quy tắc 3 (Node Lá):** Tất cả các Node lá rỗng (NIL / null leaf) luôn luôn được coi là màu **ĐEN**.
4. **Quy tắc 4 (Không có 2 Node Đỏ kề nhau):** Nếu một Node là màu **ĐỎ**, thì cả 2 Node con của nó **BẮT BUỘC phải là màu ĐEN** (Không bao giờ có quan hệ Cha Đỏ $\rightarrow$ Con Đỏ trên cùng 1 nhánh).
5. **Quy tắc 5 (Độ cao Đen bằng nhau - Black Height):** Với mọi Node bất kỳ, tất cả các đường đi từ Node đó xuống các Node lá NIL con của nó **phải chứa cùng một số lượng Node ĐEN**.

---

## 3. Cơ Chế Hoạt Động Khi Thêm Phần Tử (Insertion & Rebalancing)

Khi chèn một phần tử mới vào Cây Đỏ - Đen:
1. Node mới luôn được gán màu **ĐỎ** ban đầu (để không làm thay đổi số lượng Node Đen trên các nhánh - giữ nguyên Quy tắc 5).
2. Nếu Node mới là Node Gốc (Root) $\rightarrow$ Đổi màu thành **ĐEN** (thỏa mãn Quy tắc 2).
3. Nếu **Node Cha (Parent) là màu ĐỎ** $\rightarrow$ Vi phạm Quy tắc 4 (2 Node Đỏ liên tiếp)! Cây phải tiến hành **Tự cân bằng (Rebalancing)** qua 2 thao tác:
   * **Đổi màu (Recoloring)**
   * **Xoay cây (Rotation):** Xoay Trái (`Left Rotate`) hoặc Xoay Phải (`Right Rotate`).

---

### Các Thao Tác Xoay Cây (Rotations)

Xoay cây giúp tái cấu trúc lại các nhánh cây mà không làm phá hỏng tính chất của Cây tìm kiếm nhị phân (Node bên trái < Node cha < Node bên phải).

#### 1. Xoay Phải (Right Rotate):
```
        Y (Cha)                  X
       / \                      / \
      X   T3   ===>            T1  Y
     / \                          / \
    T1  T2                       T2  T3
```

#### 2. Xoay Trái (Left Rotate):
```
      X                          Y (Cha)
     / \                        / \
    T1  Y      ===>            X   T3
       / \                    / \
      T2  T3                 T1  T2
```

---

### Các Trường Hợp Tự Cân Bằng Khi Thêm Node Mới (3 Cases)

Xét Node mới chèn là `N`, Node Cha là `P` (Parent - Đỏ), Node Ông là `G` (Grandparent - Đen), và Node Chú là `U` (Uncle - con còn lại của G):

#### 🔴 Trường hợp 1: Node Chú (`U`) là màu ĐỎ
* **Cách xử lý:** 
  1. Đổi màu Cha (`P`) và Chú (`U`) thành **ĐEN**.
  2. Đổi màu Ông (`G`) thành **ĐỎ**.
  3. Đưa con trỏ xét vi phạm lên Node Ông (`G`) và tiếp tục kiểm tra lại.

```
       G (Black)                       G (Red)
      / \                             / \
  P(Red) U(Red)   ===>            P(Black) U(Black)
    /                               /
 N(Red)                          N(Red)
```

#### 🔴 Trường hợp 2: Node Chú (`U`) là màu ĐEN (hoặc NIL) & Dạng Zic-zac (Left-Right / Right-Left)
* **Cách xử lý:** Xoay Node Cha (`P`) để biến dạng Zic-zac thành dạng Thẳng hàng (Left-Left / Right-Right), sau đó chuyển sang **Trường hợp 3**.

```
       G (Black)                       G (Black)
      / \                             / \
  P(Red) U(Black) ===(Rotate P)===> N(Red) U(Black)
    \                               /
   N(Red)                        P(Red)
```

#### 🔴 Trường hợp 3: Node Chú (`U`) là màu ĐEN (hoặc NIL) & Dạng Thẳng hàng (Left-Left / Right-Right)
* **Cách xử lý:** 
  1. Xoay Node Ông (`G`).
  2. Đổi màu Cha (`P`) thành **ĐEN**, đổi màu Ông (`G`) thành **ĐỎ**.

```
       G (Black)                       P (Black)
      / \                             / \
  P(Red) U(Black) ===(Rotate G)===> N(Red) G (Red)
    /                                       \
 N(Red)                                    U(Black)
```

---

## 4. Tại Sao Java Chọn Cây Đỏ - Đen Cho `TreeMap`, `TreeSet` & `HashMap`?

Trong khoa học máy tính có 2 loại cây tự cân bằng phổ biến nhất là **Cây AVL (AVL Tree)** và **Cây Đỏ - Đen (Red-Black Tree)**.

| Tiêu chí | Cây AVL (AVL Tree) | Cây Đỏ - Đen (Red-Black Tree) |
| :--- | :--- | :--- |
| **Mức độ cân bằng** | Cân bằng nghiêm ngặt (chênh lệch chiều cao $\le 1$). | Cân bằng tương đối (chiều cao nhánh dài nhất $\le 2 \times$ nhánh ngắn nhất). |
| **Tốc độ Tìm kiếm (`search`)** | Nhanh hơn một chút (do cây thấp hơn). | Nhanh ($O(\log N)$). |
| **Tốc độ Thêm / Xóa (`insert/delete`)** | Chậm hơn (do tốn nhiều phép xoay cây để giữ cân bằng nghiêm ngặt). | **Nhanh hơn** (ít tốn phép xoay cây hơn, đa số chỉ cần đổi màu). |
| **Ứng dụng trong Java** | Hiếm khi dùng trong Collections. | **Mặc định** cho `TreeMap`, `TreeSet`, và `HashMap` (Java 8+). |

> **Kết luận:** Do ứng dụng thực tế đòi hỏi tần suất **Thêm/Sửa/Xóa dữ liệu liên tục**, Cây Đỏ - Đen mang lại hiệu năng cân bằng hài hòa nhất giữa Tìm kiếm ($O(\log N)$) và Cập nhật dữ liệu ($O(\log N)$).

---
*Tài liệu này hệ thống toàn bộ cơ chế của Cây Đỏ - Đen!*
