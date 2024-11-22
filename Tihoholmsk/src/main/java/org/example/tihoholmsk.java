package org.example;

import javax.sound.sampled.*; // импорт для работы со звуком
import java.awt.*; // импорт для работы с графикой
import java.awt.event.KeyEvent; // импорт для обработки нажатия клавиш
import java.awt.event.KeyListener; // импорт для обработки событий клавиатуры
import javax.swing.*; // импорт для работы с интерфейсом
import java.awt.image.BufferedImage; // импорт для работы с изображениями
import java.io.IOException; // импорт для обработки исключений ввода-вывода
import java.io.InputStream; // импорт для работы с входными потоками
import java.util.Random; // импорт для случайных чисел
import javax.imageio.ImageIO; // импорт для работы с изображениями image

public class tihoholmsk extends JFrame implements KeyListener { // создает окно приложения и обрабатывает нажатие клавиш
    private int medkits = 0; // кол-во аптечек
    private int bullets = 0; // кол-во патронов
    private int health = 100; // хп игрока
    private String message = "Вы очнулись в Тихохолмске. Найдите выход. Если сможете...";
    private int location = 0; // текущая локация (0 - стартовая, 1 - патроны, 2 - враг)
    private BufferedImage playerImage;
    private BufferedImage enemyImage;
    private BufferedImage currentMapImage;
    private Font font;
    private Clip backgroundMusic;
    private Random random;
    private boolean enemyEncountered = false; // флаг для отслеживания встречи с врагом

    public tihoholmsk() {
        setTitle("Тихохолмск"); // заголовок
        setSize(600, 400); // размер окна игры
        setDefaultCloseOperation(EXIT_ON_CLOSE); // действие по закрытию окна
        setLocationRelativeTo(null); // центрирует игру
        addKeyListener(this); // добавляет слушатель событий клавиатуры
        random = new Random(); // объект класса рандом
        loadResources(); // метод для загрузки ресов
        playBackgroundMusic("background.wav"); // метод для воспроизв. музона
        setVisible(true);
    }

    private void loadResources() {
        loadPlayerImage();
        loadEnemyImage();
        loadFont();
        loadMapImage();
    }

    private void loadPlayerImage() {
        try {
            playerImage = ImageIO.read(getClass().getResourceAsStream("/player.png")); // загрузка изображения из файла в ресах проекта
        } catch (IOException e) { // обработка возможного искл ввода-вывода (напр. если файл не найден)
            e.printStackTrace(); // вывод трассировки стека искл на консоль для отладки
        }
    }

    private void loadEnemyImage() {
        try {
            enemyImage = ImageIO.read(getClass().getResourceAsStream("/enemy.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadFont() {
        try {
            InputStream fontStream = getClass().getResourceAsStream("/shfont.ttf");
            if (fontStream != null) { // проверка успешности загрузки шрифта
                font = Font.createFont(Font.TRUETYPE_FONT, fontStream).deriveFont(20f); // указывает на тип шрифта и его размер
            } else {
                System.err.println("Font file not found!"); // ошибка если файл шрифта не найден
            }
        } catch (Exception e) { // обработка любых возмож. искл. во время загрузки и создания шрифта
            e.printStackTrace();
        }
    }

    private void loadMapImage() {
        String[] mapNames = {"map.jpg", "map1.jpg", "map2.jpg"}; // массив карт
        try {
            currentMapImage = ImageIO.read(getClass().getResourceAsStream("/" + mapNames[location])); // загрузка изобр. карты из массива в зависимости от знач. location
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void playBackgroundMusic(String filePath) {
        try {
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(getClass().getResourceAsStream("/" + filePath));
            backgroundMusic = AudioSystem.getClip(); // создание clip для воспроизв. звука
            backgroundMusic.open(audioInputStream); // открытие аудиопотока в clip
            backgroundMusic.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkForItems() {
        if (location == 1) { // локация с патронами
            bullets += 10; // добав. патроны к имеющимся
            message = "Вы нашли патроны, добавляем в инвентарь.";
        } else if (location == 0) { // локация с аптечками
            if (health < 100) {
                health = 10;
                message = "Вы нашли аптечку, восстанавливаем здоровье.";
            } else {
                medkits++;
                message = "Вы нашли аптечку! Добавляем в инвентарь.";
            }
        } else if (location == 2) { // локация с врагом
            enemyEncountered = true; // обозначение встречи с врагом
            startBattle();
        }
        repaint(); // перерисовка экрана (для отображ. изменений)
    }

    private void startBattle() {
        if (bullets > 0) {
            int chance = random.nextInt(50);
            int damage = random.nextInt(51) + 30; // урон от 50 до 80

            if (chance < 20) {
                message = "Вы смогли отбиться от врага.";
                bullets -= 1; // использование патронов
                enemyEncountered = false; // враг побежден, флаг сбрасывается
            } else {
                health -= damage;
                message = "Вас преследуют! Вы получили " + damage + " урона.";
                if (health <= 0) {
                    health = 0; // здоровье не может быть меньше 0
                    message = "Вы погибли. Попробуйте снова выбраться из Тихохолмска.";
                    gameOver();
                    restartGame();
                }
            }
        } else {
            message = "Патронов нет, вы решили сбежать.";
            bullets += 10; // находим патроны при сбеге
            enemyEncountered = false; // сбежали от врага
        }
        repaint();
    }

    private void gameOver() {
        JOptionPane.showMessageDialog(null, message,"Игра окончена.", JOptionPane.INFORMATION_MESSAGE);
    }

    private void restartGame() {
        health = 100;
        bullets = 0;
        medkits = 0;
        location = 0;
        enemyEncountered = false;
    }

    @Override // метод переопределяет объявление метода в базовом классе
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_UP) { // проверка нажата ли клавиша вверх
            location++;
            if (location > 2) location = 0; // переход на первую локацию если вышли за предел
            loadMapImage();
            checkForItems();
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) { // проверка нажата ли кнопка вниз
            location--;
            if (location < 0) location = 2; // переход на последнюю локацию если вышли за предел
            loadMapImage();
            checkForItems();
        }
    }

    @Override // переопределение метода из keylistener
    public void keyReleased(KeyEvent e) {} // метод вызывается при отпускании клавиш

    @Override // переопред метода из keylistener
    public void keyTyped(KeyEvent e) {} // метод вызывается при нажатии клавиш

    @Override // переопред метода paint из jframe
    public void paint(Graphics g) { //
        super.paint(g); // вызов метода для коррект. отрисовки
        g.setFont(font); // установка шрифта
        g.setColor(Color.BLACK);
        g.fillRect(0, 300, 620, 130); // черная рамка для текста
        g.setColor(Color.WHITE); // текст белого цвета
        g.drawString("Аптечки: " + medkits, 10, 325); // текст с кол-вом аптечек
        g.drawString("Патроны: " + bullets, 480, 320); // текст с кол-вом патронов
        g.drawString(message, 10, 310); // сообщение

        if (currentMapImage != null) { // проверка на наличие загруженной карты
            g.drawImage(currentMapImage, 0, 0, 600, 300, null); // отрисовка карты
        }

        if (playerImage != null) {
            g.drawImage(playerImage.getScaledInstance(40, 40, Image.SCALE_SMOOTH), 280, 210, null);
        }

        // Отрисовка врага, если произошла встреча
        if (enemyEncountered && enemyImage != null) {
            g.drawImage(enemyImage.getScaledInstance(80, 80, Image.SCALE_SMOOTH), 270, 100, null); // Положение врага
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(tihoholmsk::new);
    } // вызов метода new() класса tihoholmsk в потоке GUI для безопасного создания и отображения графического интерфейса
}
