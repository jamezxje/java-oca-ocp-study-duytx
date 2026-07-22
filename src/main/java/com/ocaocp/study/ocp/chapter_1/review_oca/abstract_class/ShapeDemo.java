package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Ví dụ tổng hợp về Abstract Class ứng dụng trong Hình học (Shape).
 * 
 * Lớp trừu tượng định nghĩa khung sườn (color, displayColor, calculateArea)
 * Các lớp con cụ thể (Circle, Rectangle) bổ sung các thuộc tính riêng (radius, width, height)
 * và triển khai chi tiết cách tính diện tích riêng biệt của mình.
 */

abstract class Shape {
    String color;

    // Constructor khởi tạo màu sắc cho hình
    Shape(String color) {
        this.color = color;
    }

    // Phương thức trừu tượng tính diện tích
    abstract double calculateArea();

    // Phương thức cụ thể dùng chung cho mọi hình
    void displayColor() {
        System.out.println("Hình này có màu: " + color);
    }
}

class Circle extends Shape {
    double radius;

    Circle(String color, double radius) {
        super(color);
        this.radius = radius;
    }

    @Override
    double calculateArea() {
        return Math.PI * radius * radius;
    }
}

class Rectangle extends Shape {
    double width;
    double height;

    Rectangle(String color, double width, double height) {
        super(color);
        this.width = width;
        this.height = height;
    }

    @Override
    double calculateArea() {
        return width * height;
    }
}

public class ShapeDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO TỔNG HỢP SHAPE ===");
        
        Shape circle = new Circle("Đỏ", 5.0);
        circle.displayColor();
        System.out.printf("Diện tích hình tròn: %.2f\n", circle.calculateArea());

        System.out.println("--------------------");

        Shape rectangle = new Rectangle("Xanh lá", 4.0, 6.0);
        rectangle.displayColor();
        System.out.printf("Diện tích hình chữ nhật: %.2f\n", rectangle.calculateArea());
    }
}
