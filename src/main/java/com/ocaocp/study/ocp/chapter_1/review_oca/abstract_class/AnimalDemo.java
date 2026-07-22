package com.ocaocp.study.ocp.chapter_1.review_oca.abstract_class;

/**
 * Quy tắc 1 & 2: Quy tắc kế thừa và triển khai phương thức abstract.
 * 
 * - Quy tắc 1: Lớp con cụ thể (Concrete class) BẮT BUỘC phải override (ghi đè)
 *   toàn bộ phương thức abstract của lớp cha trừu tượng.
 * - Quy tắc 2: Lớp con trừu tượng (Abstract subclass) KHÔNG CẦN override
 *   các phương thức abstract của lớp cha trừu tượng. Nhiệm vụ đó sẽ được chuyển giao
 *   cho lớp con cụ thể đầu tiên kế thừa nó.
 */

// Lớp cha trừu tượng bậc 1
abstract class Animal {
    // Phương thức trừu tượng
    abstract void makeSound();
}

// Quy tắc 2: Lớp con trừu tượng (Abstract subclass) - Không cần implement makeSound()
abstract class Mammal extends Animal {
    // Có thể khai báo thêm phương thức trừu tượng mới
    abstract void nurseYoung();
}

// Quy tắc 1: Lớp con cụ thể (Concrete Class) - Phải implement toàn bộ phương thức abstract từ các lớp cha
class Dog extends Mammal {

    // Triển khai phương thức của Animal
    @Override
    void makeSound() {
        System.out.println("Dog says: Gâu gâu!");
    }

    // Triển khai phương thức của Mammal
    @Override
    void nurseYoung() {
        System.out.println("Dog is nursing its puppies with milk.");
    }
}

public class AnimalDemo {
    public static void main(String[] args) {
        System.out.println("=== DEMO QUY TẮC 1 & 2 ===");
        
        // Animal animal = new Animal(); // LỖI BIÊN DỊCH: Không thể khởi tạo abstract class trực tiếp.
        
        Dog dog = new Dog();
        dog.makeSound();
        dog.nurseYoung();
    }
}
