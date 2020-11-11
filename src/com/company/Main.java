//Загвозкин Артём, ArtZagvozkin@yandex.ru

package com.company;

import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Main {

    //Параметры игры
    int PointRadius = 1; //Радиус живой клетки
    Color PointColor = Color.black;  //Цвет живой клетки
    Color BackgroundColor = Color.white; //Цвет фона вселенной
    int Height = 654; //Количество клеток по высоте
    int Width = 1360; //Количество клеток по ширине

    //Основной функционал
    boolean[][] CurrentWorld = new boolean[Width][Height]; //Состояние текущей всленной
    boolean[][] NextWorld = new boolean[Width][Height]; //Следующая вселенная
    Canvas CanvasWorld = new Canvas(); //Канва для отображения вселенной

    //Дополнительный функционал
    volatile boolean isGenerate = false; //Ключ, который разрешает генерацию нового поколения. Для реализации паузы
    int TimeSec = 0; //Таймер
    int CountFrame = 0; //Количество сгенерированных кадров на данный момент
    int PrevCountFrame = 0; //Количество сгенерированных кадров 1 сек назад. Для расчета FPS(FPS = CountFrame - PrevCountFrame)

    public static void main(String[] args) {
        //Запускаем метод, который отрисовывает frame
        new Main().showFrame();
    }

    //Запуск и отрисовка окна
    void showFrame() {
        //Создаём и описываем окно
        JFrame frame = new JFrame("Game of Life"); //Создаем frame с заголовком "Game of Life"
        frame.setSize(Width * PointRadius + 16, Height * PointRadius + 67); //Размер окна
        frame.setResizable(false); //Запрет на изменение размера окна
        frame.setLocationRelativeTo(null); //Окно по центру
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //Завершение программы при закрытии окна
        CanvasWorld.setBackground(BackgroundColor); //Задаём фон вселенной


        //Информационный блок состояния вселенной
        JLabel LblTime = new JLabel("Time: ");
        JLabel LblFPS = new JLabel("FPS: ");
        JLabel LblFrame = new JLabel("Frame: ");
        LblFrame.setBorder(BorderFactory.createEmptyBorder(5,5,5,5)); //Пытаемся выровнять по центру
        //Помещаем 3 JLabel с текстом в JPanel
        JPanel PnlInfo = new JPanel(new FlowLayout(FlowLayout.LEFT, 30, 0));
        PnlInfo.setBackground(BackgroundColor);
        PnlInfo.add(LblTime);
        PnlInfo.add(LblFPS);
        PnlInfo.add(LblFrame);


        //Кнопки
        JButton BtnRand = new JButton("Random");
        JButton BtnLoad = new JButton("Load");
        JButton BtnClear = new JButton("Clear");
        JButton BtnStart = new JButton("Start");
        //Нажатие на кнопку "Random"
        BtnRand.addActionListener(e -> {
            //Генерируем поле
            Random rand = new Random();
            for (int x = 0; x < Width; x++)
                for (int y = 0; y < Height; y++)
                    CurrentWorld[x][y] = rand.nextBoolean();
            //Обновляем переменные состояния и информационный блок
            TimeSec = 0;
            CountFrame = 0;
            PrevCountFrame = 0;
            LblTime.setText("Time: 0");
            LblFPS.setText("FPS: 0");
            LblFrame.setText("Frame: 0");
            BtnStart.setText("Start");
            //Перерисовываем вселенную
            CanvasWorld.repaint();
        });
        //Нажатие на кнопку "Load"
        BtnLoad.addActionListener(e -> {
            //Загрузка из файла
        });
        //Нажатие на кнопку "Clear"
        BtnClear.addActionListener(e -> {
            for (int x = 0; x < Width; x++)
                for (int y = 0; y < Height; y++)
                    CurrentWorld[x][y] = false;
            //Обновляем переменные состояния и информационный блок
            TimeSec = 0;
            CountFrame = 0;
            PrevCountFrame = 0;
            LblTime.setText("Time: 0");
            LblFPS.setText("FPS: 0");
            LblFrame.setText("Frame: 0");
            BtnStart.setText("Start");
            //Перерисовываем вселенную
            CanvasWorld.repaint();
        });
        //Нажатие на кнопку "Start"
        BtnStart.addActionListener(e -> {
            //Меняем состояние ключа, который разрешает или запрещает генерацию
            isGenerate = !isGenerate;
            //Меняем надпись на "Pause" или "Resume"
            BtnStart.setText(isGenerate ? "Pause" : "Resume");
            //Деактивация/активация кнопок
            BtnRand.setEnabled(!isGenerate);
            BtnLoad.setEnabled(!isGenerate);
            BtnClear.setEnabled(!isGenerate);
        });


        //Создаем JMenuBar, в который помещаем кнопки и информационный блок
        JMenuBar menuBar = new JMenuBar();
        menuBar.add(BtnRand);
        menuBar.add(BtnLoad);
        menuBar.add(BtnClear);
        menuBar.add(BtnStart);
        menuBar.add(PnlInfo);

        //Помещаем CanvasWorld и menuBar в frame
        frame.setJMenuBar(menuBar);
        frame.getContentPane().add(BorderLayout.CENTER, CanvasWorld);


        //Делаем frame видимым
        frame.setVisible(true);

        //Запускаем таймер
        TimerTask tmrTask = new TimerTask() {
            @Override
            public void run() {
                if (isGenerate) {
                    TimeSec++;
                    LblTime.setText("Time: " + TimeSec);
                    LblFPS.setText("FPS: " + (CountFrame - PrevCountFrame));
                    PrevCountFrame = CountFrame;
                }
            }
        };
        Timer tmr = new Timer();
        tmr.schedule(tmrTask, 1000, 1000);

        //Запускаем игру
        while (true) if (isGenerate) {
            generateNextFrame();
            LblFrame.setText("Frame: " + CountFrame);
            CanvasWorld.repaint();
        }
    }

    //Формируем следующий кадр
    void generateNextFrame() {
        for (int x = 0; x < Width; x++)
            for (int y = 0; y < Height; y++) {
                int count = countNeighbors(x, y);
                //Условие существования ячейки. 3 или 4, поскольку сама ячейка тоже считается
                NextWorld[x][y] = (!CurrentWorld[x][y] && count == 3) || (CurrentWorld[x][y] && (count == 3 || count == 4));
            }
        //Копируем новое поколение в текущее
        for (int x = 0; x < Width; x++)
            System.arraycopy(NextWorld[x], 0, CurrentWorld[x], 0, Height);
        //Делаем инкремент поколению
        CountFrame++;
    }

    //Считаем количество сосдей
    int countNeighbors(int x, int y) {
        int count = 0;
        for (int dx = -1; dx < 2; dx++)
            for (int dy = -1; dy < 2; dy++) {
                int nX = x + dx;
                int nY = y + dy;
                //Решаем, нужно ли перескочить на противоположный край экрана?
                nX = (nX < 0) ? Width - 1 : nX;
                nY = (nY < 0) ? Height - 1 : nY;
                nX = (nX > Width - 1) ? 0 : nX;
                nY = (nY > Height - 1) ? 0 : nY;
                //Инкремент количества соседей
                count += (CurrentWorld[nX][nY]) ? 1 : 0;
            }
        return count;
    }

    //Отрисовываем мир
    public class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            //Задаем цвет точки
            g.setColor(PointColor);
            //Распологаем живые клетки по канве
            for (int x = 0; x < Main.this.Width; x++)
                for (int y = 0; y < Main.this.Height; y++)
                    if (CurrentWorld[x][y])
                        g.fillRect(x * PointRadius, y * PointRadius, PointRadius, PointRadius);
        }
    }
}
