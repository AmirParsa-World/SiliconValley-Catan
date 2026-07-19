# Silicon Valley Catan

A board game inspired by Catan, re-themed around the Silicon Valley startup ecosystem. Built with JavaFX.

## How to Run

1. Double-click **`run-game.bat`**
2. Select number of players (2-4)
3. For each player, choose **Human** or **Bot**
4. Play the game!

**Prerequisites:** JDK 17 and JavaFX 21 SDK (already included in the project downloads).

## How to Compile 

Double-click **`compile.bat`**

## Project Structure

```
src/
├── model/          # Game data model
│   ├── Map.java          # 5x5 sector grid with vertices and edges
│   ├── Sector.java       # Resource tiles with activation numbers
│   ├── Vertex.java       # Intersections where structures are placed
│   ├── Edge.java         # Connections between vertices (roads)
│   ├── Player.java       # Player state, wallet, structures
│   ├── SimpleBot.java    # AI bot player
│   ├── Structure.java    # Abstract base for buildings
│   ├── MVP.java          # Minimum Viable Product (1 point)
│   ├── Unicorn.java      # Unicorn startup (2 points)
│   ├── Dice.java         # Two-d6 dice with individual die tracking
│   ├── ResourceType.java # DATA, PATENT, CLOUD, CAPITAL, TALENT, REGULATORY
│   ├── FounderRole.java  # HACKER_CEO, GURU_CTO, VC_FUNDED, NONE
│   └── Regulator.java    # Auditor blocker position
├── controller/     # Game logic
│   ├── GameEngine.java   # Core game engine (turns, builds, trades)
│   ├── GamePhase.java    # SETUP, NORMAL, FINISHED
│   └── Market.java       # Dynamic market with price fluctuations
├── view/           # JavaFX GUI
│   ├── MainApp.java      # Application entry point, bot orchestration
│   ├── BoardCanvas.java  # Game board with sectors, vertices, edges
│   ├── DicePane.java     # Canvas-drawn dice with dot patterns
│   ├── PlayerInfoPane.java # Player cards with resource breakdown
│   ├── ActionPane.java   # Action buttons and status display
│   └── MarketPane.java   # Market price display
├── exception/      # Custom exceptions
└── util/           # Save/load and testing utilities
```

## Game Rules

### Setup Phase (Snake Draft)
- Players take turns placing one MVP + one Partnership (road)
- Draft order: forward then backward (e.g., 3 players: [0,1,2,2,1,0])

### Normal Phase
1. Roll two dice (2-12)
2. Sectors matching the roll produce resources for adjacent structures
3. Roll of 7 triggers Regulatory Crisis (discard if over limit, move auditor)
4. Build structures:
   - **MVP**: 1 Capital + 1 Talent + 1 Cloud + 1 Data → 1 point
   - **Partnership**: 1 Capital + 1 Patent → road connection
   - **Unicorn**: 3 Data + 2 Cloud (1 Cloud if Guru CTO) → upgrade MVP to 2 points

### Victory
First player to **10 points** wins.

### Founder Roles (optional, -1 point if chosen)
- **Hacker CEO**: 3:1 market trade rate (instead of 4:1)
- **Guru CTO**: Cheaper Unicorn upgrades (1 Cloud instead of 2)
- **VC Funded**: 9 card holding limit (instead of 7)


🚀 پروژه پایانی درس برنامه‌نویسی پیشرفته: بازی Silicon Valley Catan
این پروژه یک نسخه بومی‌سازی شده و خلاقانه از بازی رومیزی محبوب کتان (Catan) با تم دنیای استارتاپ‌ها و شبیه‌سازی فضای رقابتی سیلیکون ولی است. معماری پروژه بر اساس اصول مهندسی نرم‌افزار و الگوی طراحی MVC پایه‌ریزی شده و تمام منطق پردازشی (Back-end) به صورت کاملاً مستقل از لایه گرافیک (Front-end) توسعه یافته است.

🎮 ۱. توضیح کلی منطق و مکانیزم‌های بازی
در این بازی، بازیکنان به عنوان بنیان‌گذاران استارتاپ‌ها (Founders) برای تصاحب بازار تکنولوژی رقابت می‌کنند.
💎 منابع و تایل‌های نقشه
به جای منابع سنتی (گندم، چوب و...)، نقشه بازی از سکتورهای فناوری تشکیل شده است که منابع زیر را تولید می‌کنند:
Data (Valley): داده‌های حیاتی برای توسعه هوش مصنوعی.
Patent (IP Quarter): پتنت‌ها و حقوق مالکیت معنوی.
Cloud (Campus): زیرساخت‌های ابری.
Capital (Fintech): سرمایه نقدی که واحد پول بازار نیز هست.
Talent (AI Hub): جذب نیروهای متخصص و نخبه.
💼 سیستم داینامیک نقش‌ها (Founder Roles)
هر بازیکن در ابتدای بازی می‌تواند یک نقش استارتاپی منحصر‌به‌فرد با مزایای مکانیکی دائمی انتخاب کند:
The Hacker CEO: معامله مستقیم در بازار با نرخ ۳:۱ (به جای ۴:۱) و خرید کالا با قیمت ثابت ۳ واحد سرمایه.
The Tech Guru (CTO): تخفیف ویژه در ارتقای ساختارها (نیاز به ۱ واحد زیرساخت ابری کمتر برای ارتقا به تک‌شاخ).
The VC-Funded: شروع بازی با +۲ سرمایه بیشتر و سقف مجاز نگهداری ۹ کارت (به جای ۷ کارت) در هنگام بحران مالیاتی.
📈 اقتصاد پویا و مارکت هوشمند (Dynamic Market Engine)
برخلاف کتان سنتی، قیمت منابع بر اساس قانون عرضه و تقاضا نوسان می‌کند:
خرید کالا: قیمت آن منبع را ۱ واحد افزایش می‌دهد (افزایش تقاضا).
فروش کالا: قیمت آن منبع را ۱ واحد کاهش می‌دهد (افزایش عرضه).
ساعت رکود (Stagnation Clock): اگر یک منبع به مدت ۳ راند متوالی هیچ تراکنشی (خرید یا فروش) نداشته باشد، حباب قیمت آن شکسته شده و سیستم به طور خودکار قیمت آن را ۱ واحد کاهش می‌دهد تا تعادل به بازار برگردد.
🕵️‍♂️ بازرس مالیاتی (Auditor) و بحران مالیاتی
با آمدن تاس ۷، بحران مالیاتی رخ می‌دهد. بازیکنانی که بیش از حد مجاز (۷ یا ۹ کارت) دست خود نگه داشته‌اند، باید نیمی از کارت‌های خود را بسوزانند. سپس بازیکن فعال، بازرس مالیاتی (Auditor) را به یک سکتور منتقل می‌کند تا تولید منابع آن بخش را قفل کند. سیستم دارای لایه امنیتی است و اجازه قرارگیری بازرس در سکتورهای کاملاً خالی را نمی‌دهد.
🏆 الگوریتم شبکه ارتباطی طولانی (Longest Network)
سیستم با استفاده از الگوریتم جستجوی عمق-اول (DFS) روی گراف لبه‌های متعلق به هر بازیکن، به صورت کاملاً پویا طولانی‌ترین مسیر متصلِ بدون هم‌پوشانی را محاسبه کرده و پاداش ۲ امتیازی بزرگ‌ترین شبکه را به بازیکن برتر اهدا می‌کند.
🏛 ۲. ساختار کلاس‌ها و معماری سیستم (UML Breakdown)
 
    🧠 توضیحات کلیدی متدهای کنترلر جهت ارزیابی:
GameEngine.executePeerTrade(...): متد اختصاصی معامله آزاد و توافقی بین دو بازیکن حقیقی که تمام اعتبارسنجی‌های موجودی در آن رعایت شده است.
Market.incrementRoundTick(): جلو بردن راند بازار و اعمال قانون افت قیمت در صورت رکود ۳ دوره‌ای.
GameEngine.calculateLongestNetwork(): راهاندازی ساختار الگوریتم DFS برای پیدا کردن طولانی‌ترین جاده.

💻 ۳. نحوه اجرای پروژه
برای اجرای بدون مشکل پروژه، مطمئن شوید که JDK 11 یا بالاتر روی سیستم شما نصب است.
روش اول: اجرا از طریق ترمینال / خط فرمان
۱. ابتدا فایل zip پروژه را استخراج کرده و وارد پوشه اصلی شوید.
۲. با دستور زیر سورس‌کدها را کامپایل کنید:
Bash
javac -d bin src/**/*.java
۳. با دستور زیر برنامه را اجرا کنید (نام کلاس Main پروژه خود را جایگزین کنید):
Bash
java -cp bin view.GameWindow
روش دوم: اجرا در محیط‌های IDE (IntelliJ IDEA / Eclipse)
۱. پوشه پروژه را به عنوان یک پروژه Java در IDE خود Open کنید.
۲. مطمئن شوید پوشه src به عنوان پوشه اصلی سورس‌کدها (Source Root) شناسایی شده است.
۳. روی کلاس اصلی گرافیکی (مثلاً GameWindow.java یا کلاس دارای متد main) راست کلیک کرده و گزینه Run را انتخاب کنید.
👥 ۴. تقسیم کار بین اعضای گروه
توسعه این پروژه به صورت کاملاً چابک  صورت گرفته و وظایف به شرح زیر بین اعضای گروه تقسیم شده است:

امیرپارسا جهانگیر 
[4042262131]
طراحی لایه هسته و منطق اصلی بازی (GameEngine)، پیاده‌سازی مکانیزم نوبت‌دهی مارپیچ (Snake Draft) و سیستم مدیریت نقش‌ها.

پیاده‌سازی الگوریتم گراف طولانی‌ترین جاده (DFS Network)، توسعه ساختارهای داده نقشه 

طراحی و توسعه شبیه‌ساز اقتصادی بازار پیاپی (Market)، منطق نوسان قیمت‌ها و ساعت رکود، پیاده‌سازی متدهای اعتبارسنجی ترید.

ریحانه اکبرنژاد 
[4042262084]
پیاده‌سازی فرانت‌اند و رابط کاربری گرافیکی (UI)، نقشه‌کشی داینامیک تایل‌ها بر اساس مختصات پیکسل‌ها و اتصال رویداد دکمه‌ها به کنترلر.

(Sector, Vertex, Edge) و سیستم توزیع منابع تاس و توسعه ساختارهای داده نقشه

