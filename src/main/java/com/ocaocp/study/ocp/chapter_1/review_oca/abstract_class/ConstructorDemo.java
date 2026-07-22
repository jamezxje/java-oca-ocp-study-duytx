package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Quy tắc 5: Quy tắc về Constructor và từ khóa 'super' trong Abstract Class.
 * 
 * - Mặc dù abstract class không thể được khởi tạo trực tiếp bằng từ khóa 'new', nó VẪN CÓ constructor.
 * - Constructor này được dùng để khởi tạo trạng thái (biến thành viên) của lớp cha khi một lớp con cụ thể được tạo.
 * - Khi lớp con khởi tạo, nó luôn gọi constructor của lớp cha đầu tiên (ngầm định gọi super() hoặc gọi tường minh super(...)).
 */

abstract class Employee {
    String name;
    String department;

    // Constructor của lớp abstract dùng để gán các giá trị cơ bản
    Employee(String name, String department) {
        System.out.println("1. Constructor của lớp cha Employee được gọi.");
        this.name = name;
        this.department = department;
    }

    // Phương thức cụ thể hiển thị thông tin
    void displayInfo() {
        System.out.println("Employee Name: " + name + ", Dept: " + department);
    }
}

class Developer extends Employee {
    String primaryLanguage;

    Developer(String name, String department, String primaryLanguage) {
        // Bắt buộc gọi constructor của lớp cha trước khi thực hiện logic khác của lớp con
        super(name, department); 
        System.out.println("2. Constructor của lớp con Developer được gọi.");
        this.primaryLanguage = primaryLanguage;
    }

    void showDetails() {
        displayInfo();
        System.out.println("Primary Programming Language: " + primaryLanguage);
    }
}

public class ConstructorDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO QUY TẮC 5 ===");
        Developer dev = new Developer("Tran Xuan Duy", "R&D", "Java");
        System.out.println("--------------------");
        dev.showDetails();
    }
}
