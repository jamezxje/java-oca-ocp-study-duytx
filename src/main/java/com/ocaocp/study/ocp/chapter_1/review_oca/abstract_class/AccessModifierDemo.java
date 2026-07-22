package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Quy tắc 3: Quy tắc về Phạm vi truy cập (Access Modifier) khi ghi đè.
 * 
 * - Khi ghi đè (override) phương thức ở lớp con (bao gồm cả việc triển khai phương thức abstract),
 *   bạn KHÔNG ĐƯỢC PHÉP giảm mức độ truy cập (visibility) của phương thức so với lớp cha.
 * - Thứ tự phạm vi truy cập từ rộng đến hẹp: public -> protected -> default (package-private) -> private.
 * - Ví dụ: Nếu lớp cha khai báo protected abstract, lớp con chỉ được ghi đè dưới dạng protected hoặc public.
 */

abstract class Service {
    // 1. Phương thức abstract phạm vi public
    public abstract void start();

    // 2. Phương thức abstract phạm vi protected
    protected abstract void configure();

    // 3. Phương thức abstract phạm vi default (package-private)
    abstract void execute();

    // LƯU Ý: Không thể khai báo 'private abstract' vì private ngăn cản việc ghi đè (Quy tắc 4).
}

class DatabaseService extends Service {

    // HỢP LỆ: Giữ nguyên public
    @Override
    public void start() {
        System.out.println("Database service started.");
    }

    // HỢP LỆ: Tăng mức độ truy cập từ protected lên public (hoặc giữ nguyên protected)
    @Override
    public void configure() {
        System.out.println("Database service configured with protected override.");
    }

    // HỢP LỆ: Tăng mức độ truy cập từ default lên protected (hoặc public, hoặc giữ nguyên default)
    @Override
    protected void execute() {
        System.out.println("Database service execution initiated.");
    }

    /*
    // LỖI BIÊN DỊCH: Giảm mức độ truy cập từ protected xuống default
    @Override
    void configure() { } 

    // LỖI BIÊN DỊCH: Giảm mức độ truy cập từ public xuống protected/default/private
    @Override
    protected void start() { }
    */
}

public class AccessModifierDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO QUY TẮC 3 ===");
        Service dbService = new DatabaseService();
        dbService.start();
        dbService.configure();
        dbService.execute();
    }
}
