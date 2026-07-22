package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Trình điều phối trung tâm (Demo Runner)
 * 
 * Lớp này sẽ chạy lần lượt các kịch bản thực hành từ Quy tắc 1 đến Quy tắc 5
 * để bạn quan sát kết quả hoạt động và so sánh trực tiếp lý thuyết đã học.
 */
public class Demo {
    public static void main(String[] args) {
        System.out.println("==============================================================");
        System.out.println("       CHƯƠNG TRÌNH DEMO CHUYÊN SÂU VỀ ABSTRACT CLASS         ");
        System.out.println("==============================================================");

        // 1. Chạy ShapeDemo (Tổng quan)
        System.out.println("\n--- 1. Ví dụ tổng hợp (ShapeDemo) ---");
        ShapeDemo.main(args);

        // 2. Chạy AnimalDemo (Quy tắc 1 & 2)
        System.out.println("\n--- 2. Quy tắc 1 & 2: Kế thừa & Triển khai (AnimalDemo) ---");
        AnimalDemo.main(args);

        // 3. Chạy AccessModifierDemo (Quy tắc 3)
        System.out.println("\n--- 3. Quy tắc 3: Phạm vi truy cập (AccessModifierDemo) ---");
        AccessModifierDemo.main(args);

        // 4. Chạy IllegalModifiersDemo (Quy tắc 4)
        System.out.println("\n--- 4. Quy tắc 4: Các bổ từ không hợp lệ (IllegalModifiersDemo) ---");
        IllegalModifiersDemo.main(args);

        // 5. Chạy ConstructorDemo (Quy tắc 5)
        System.out.println("\n--- 5. Quy tắc 5: Constructor & từ khóa 'super' (ConstructorDemo) ---");
        ConstructorDemo.main(args);
        
        System.out.println("\n==============================================================");
        System.out.println("  KẾT THÚC DEMO - CHÚC BẠN THI ĐẠT CHỨNG CHỈ OCA/OCP XUẤT SẮC!");
        System.out.println("==============================================================");
    }
}
