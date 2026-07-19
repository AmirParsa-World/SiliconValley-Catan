package model;

public class SimpleBot extends Player {
    private static final long serialVersionUID = 1L;

    // 🏗️ Constructor
    public SimpleBot(String name, String color) {
        super(name, color);
        // ربات‌ها به طور پیش‌فرض نقش معمولی (NONE) دارند تا تخفیف خاصی نداشته باشند
        // البه ک در ادامه تنظیم کردیم تا بتونه اختیاری نقش هم انتخاب کنه
        this.setRole(FounderRole.NONE);
    }
}