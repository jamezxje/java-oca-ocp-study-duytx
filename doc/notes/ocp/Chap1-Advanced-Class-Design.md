A. Review OCA

I. Access modifiers <Phạm vi truy cập>
    - Dùng để xác định phạm vi truy cập của các thành phần (class, variable, method) trong Java.
    - public > protected > default > private
        + public: có thể được truy cập từ bất kì đâu.
        + protected: truy cập trong cùng package và các lớp con (kể các lớp con ở package khác)
        + default: truy cập được trong cùng package
        + private: chỉ truy cập được trong cùng 1 class
    
![Bảng tóm tắt phạm vi truy cập](${workspaceFolder}/doc/assets/image-3.png)

II. Overloading và Overriding
    1. Overloading(nạp chồng phương thức)
        - Bản chất: Compile-time polymorphism (đa hình tại thời điểm biên dịch)
        - Định nghĩa: Nhiều phương thức có cùng tên nhưng khác nhau về danh sách tham số (khác kiểu dữ liệu hoặc số lượng tham số)
        - Quy tắc:  
            + Phải khác tham số đầu vào.
            + Kiểu trả về có thể giống hoặc khác nhau.
            + Modifier có thể thay đổi

    2. Overriding(ghi đè phương thức)
        - Bản chất: Runtime polymorphism (đa hình tại thời điểm chạy)
        - Định nghĩa: lớp con cung cấp cách triển khai cụ thể cho một phương thức ở lớp cha
        - Quy tắc: 
            + Tên và tham số phải giống hệt lớp cha
            + Kiểu trả về phải giống hoặc là kiểu con
            + Access modifier không được bé hơn lớp cha
            + Exception: không được ném ra checked exception mới hoặc rộng hơn lớp cha
            + Không thể override: static method(chỉ có hiding), final method, private method.

![Bảng so sánh nhanh](${workspaceFolder}/doc/assets/image-5.png)


III. Abstract Classes
    1. Định nghĩa và đặc điểm
        - Định nghĩa: được đánh dấu bằng từ khóa "abstract", đóng vai trò là lớp nền tảng cho các lớp khác kế thừa
        - Khởi tạo: lớp trừu tượng không thể tự khởi tạo trực tiếp thành đối tượng (không thể sử dụng từ khóa new)
        - Có thể chứa: 
            + Abtract methods(phương thức trừu tượng): chỉ khai báo tên, không có thân hàm. Các lớp con BẮT BUỘC phải override lại những phương thức này.
            + Concrete methods(phương thức cụ thể): phương thức có thân hàm bình thường, các lớp con có thể tái sử dụng các phương thức này.
            + Hoặc nó có thể không có phương thức nào.

    2. Quy tắc kế thừa và triển khai
        - Lớp con cụ thể(không có từ khóa "abstract") BẮT BUỘC phải ghi đè tất cả các phương thức abstract. Nếu không triển khai thì chương trình sẽ báo lỗi compile-time.
        - Lớp con trừu tượng (có từ khóa "abstract") KHÔNG BẮT BUỘC phải ghi đè các abstract method của lớp cha
        - Khi ghi đè phương thức ở lớp con thì không được phép giảm access modifier
        - Abstract method KHÔNG ĐƯỢC phép là private, static hoặc final
        - Lớp con khi khởi tạo sẽ luôn gọi constructor của lớp cha đầu tiên (ngầm định gọi super() hoặc bạn phải gọi tường
     minh bằng super(...)).
    
