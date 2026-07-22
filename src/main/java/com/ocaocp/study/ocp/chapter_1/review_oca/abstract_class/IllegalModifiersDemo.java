package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Quy tắc 4: Các sửa đổi không hợp lệ (Illegal Modifiers) với phương thức abstract.
 * 
 * Phương thức abstract sinh ra để được ghi đè bởi lớp con. Do đó, bất kỳ sửa đổi nào ngăn cản việc ghi đè
 * hoặc không tương thích với tính đa hình đều là BẤT HỢP LỆ và gây ra lỗi biên dịch:
 * 
 * 1. private abstract: private giới hạn phạm vi trong lớp, lớp con không thấy để ghi đè.
 * 2. final abstract: final cấm lớp con ghi đè.
 * 3. static abstract: static thuộc về lớp chứ không phải đối tượng, không áp dụng đa hình (override).
 */

abstract class BaseDemo {
    
    // Phương thức abstract hợp lệ
    abstract void validMethod();

    /*
    // --- CÁC TRƯỜNG HỢP GÂY LỖI BIÊN DỊCH (Đã được comment lại để chương trình chạy được) ---
    
    // 1. Lỗi: private không thể đi kèm abstract
    private abstract void illegalPrivate();

    // 2. Lỗi: final không thể đi kèm abstract
    final abstract void illegalFinal();

    // 3. Lỗi: static không thể đi kèm abstract
    static abstract void illegalStatic();
    */
}

class ConcreteDemo extends BaseDemo {
    @Override
    void validMethod() {
        System.out.println("Triển khai thành công phương thức abstract hợp lệ.");
    }
}

public class IllegalModifiersDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO QUY TẮC 4 ===");
        System.out.println("Nhớ kỹ: Một phương thức KHÔNG THỂ vừa abstract vừa: private, final, hoặc static.");
        
        ConcreteDemo demo = new ConcreteDemo();
        demo.validMethod();
    }
}
