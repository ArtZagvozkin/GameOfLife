//Загвозкин Артём, ArtZagvozkin@yandex.ru

package com.company;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.io.*;
import java.util.*;
import java.util.Timer;

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
    JFrame MainFrame = new JFrame("Game of Life"); //Создаем frame с заголовком "Game of Life"

    //Дополнительный функционал
    volatile boolean isGenerate = false; //Ключ, который разрешает генерацию нового поколения. Для реализации паузы
    int TimeSec = 0; //Таймер
    int CountFrame = 0; //Количество сгенерированных кадров на данный момент
    int PrevCountFrame = 0; //Количество сгенерированных кадров 1 сек назад. Для расчета FPS(FPS = CountFrame - PrevCountFrame)

    public static void main(String[] args) {
        //Запускаем метод, который отрисовывает frame
        new Main().showMainFrame();
    }

    //Запуск и отрисовка главного окна
    @SuppressWarnings("InfiniteLoopStatement")
    void showMainFrame() {
        //Описываем главный фрейм
        MainFrame.setResizable(false); //Запрет на изменение размера окна
        MainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);  //Завершение программы при закрытии окна
        MainFrame.setMinimumSize(new Dimension(600,100)); //Задаём минимальные размеры окна (600*100)
        MainFrame.getContentPane().add(CanvasWorld, BorderLayout.CENTER); //Помещаем CanvasWorld в frame
        MainFrame.setBackground(Color.LIGHT_GRAY); //Фон окна
        CanvasWorld.setBackground(Color.WHITE); //Фон вселенной
        resizeWorld(Width, Height);

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
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Select the file with the extension '.rle'");
            //Режим только каталог
            fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
            //Фильтр расширений
            fileChooser.setFileFilter(new FileNameExtensionFilter(".rle files", "rle"));
            //Открывает окно выбора файла
            int result = fileChooser.showOpenDialog(null);
            if (result == JFileChooser.APPROVE_OPTION) {//Если директория выбрана, то пакажем ее в заголовке окна
                //Предварительно очищаем мир
                for (int x = 0; x < Width; x++)
                    for (int y = 0; y < Height; y++)
                        CurrentWorld[x][y] = false;
                //Загружаем файл и рисуем
                rleToMatrix(fileChooser.getSelectedFile());
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
            }
            else { //Иначе выводим сообщение об ошибке
                JOptionPane.showMessageDialog(null, "No file selected.");
            }
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
        //Помещаем menuBar в frame
        MainFrame.setJMenuBar(menuBar);

        //Делаем frame видимым
        MainFrame.setVisible(true);

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

    //Задаем размер мира
    void resizeWorld(int x, int y) {
        Width = x;
        Height = y;
        PointRadius = Math.max(1, Math.min(1360 / x, 654 / y));
        CanvasWorld.setSize(Width * PointRadius, Height * PointRadius);

        MainFrame.setSize(Width * PointRadius + 16, Height * PointRadius + 67); //Размер окна
        MainFrame.setLocationRelativeTo(null); //Окно по центру
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

    //Парсинг файла .rle
    void rleToMatrix(File inFile) {

        try {
            FileReader rleFile = new FileReader(inFile);
            //Создаём BufferedReader для построчного считывания
            BufferedReader rleReader = new BufferedReader(rleFile);

            //Реализуем построчное чтение файла .rle
            String line = rleReader.readLine();  //Получаем первую строку
            boolean isReadLine = true; //Ключ, который разрешает чтение строки, пока не встретится символ конца файла
            //Начинаем в цикле построчно обрабатывать файл
            int countCells = 0; //Накапливаем количество ячеек
            int iX = 9, iY = 9;
            while (line != null && isReadLine) {
                //Пропускаем строки с комментариями #
                if (line.charAt(0) == '#')
                {
                    line = rleReader.readLine();
                    continue;
                }
                //Удаляем все пробелы
                line = line.replace(" ", "");

                //Парсим строку, которую содержит размеры окна(x*y)
                if (line.charAt(0) == 'x') {
                    String sX, sY;
                    //Получаем значения X и Y
                    sX = line.substring(line.indexOf('x') + 2, line.indexOf(','));
                    sY = line.substring(line.indexOf('y') + 2);
                    //Задаем новые параметры окна
                    try {
                        //Проверка на допустимые параметры X и Y
                        if (Integer.parseInt(sX) > 10000 || Integer.parseInt(sY) > 1200 || Integer.parseInt(sX) == 0 || Integer.parseInt(sY) == 0)
                        {
                            JOptionPane.showMessageDialog(null, "Ошибка. Недопустимый размер поля.");
                            break;
                        }
                        //Задаем размер мира
                        resizeWorld(Integer.parseInt(sX) + 18, Integer.parseInt(sY) + 18);
                    }
                    catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(null, "Не удалось загрузить файл. Неверный формат файла .rle.");
                        break;
                    }
                    line = rleReader.readLine();
                    continue;
                }
                //Парсим расположение живых клеток в очередной строке. Поисмвольный анализ строки с помощью for
                for (int i = 0; i < line.length(); i++)
                {
                    switch (line.charAt(i)) {
                        case 'b' -> { //Клетка мертва
                            countCells = (countCells == 0) ? 1 : countCells;
                            for (int j = iX; j < iX + countCells; j++)
                                CurrentWorld[j][iY] = false;
                            iX += countCells;
                            countCells = 0;
                        }
                        case 'o' -> { //Клетка жива
                            countCells = (countCells == 0) ? 1 : countCells;
                            for (int j = iX; j < iX + countCells; j++)
                                CurrentWorld[j][iY] = true;
                            iX += countCells;
                            countCells = 0;
                        }
                        case '$' -> { //Переход на новую строку
                            iX = 9;
                            if (countCells > 1)
                                iY += countCells - 1;
                            else
                                iY++;
                            countCells = 0;
                        }
                        case '!' -> //Завершаем чтение файла
                                isReadLine = false;
                        case '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> //Парсим цифры
                                countCells = countCells * 10 + Character.getNumericValue(line.charAt(i));
                        default ->
                                throw new IllegalStateException("Unexpected value: " + line.charAt(i));
                    }
                }
                //Переходим на следующую строку
                line = rleReader.readLine();
            }
            //Перерисовываем мир
            CanvasWorld.repaint();
        }
        catch (FileNotFoundException ignored) {

        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    //Отрисовываем мир
    public class Canvas extends JPanel {
        @Override
        public void paint(Graphics g) {
            super.paint(g);
            //Задаем цвет точки
            g.setColor(PointColor);
            //Распологаем живые клетки на канве
            for (int x = 0; x < Main.this.Width; x++)
                for (int y = 0; y < Main.this.Height; y++)
                    if (CurrentWorld[x][y])
                        g.fillRect(x * PointRadius, y * PointRadius, PointRadius, PointRadius);
            //Рисуем границу
            g.drawLine(Main.this.Width * PointRadius, 0, Main.this.Width * PointRadius, Main.this.Height * PointRadius);
            g.drawLine(0, Main.this.Height * PointRadius, Main.this.Width * PointRadius, Main.this.Height * PointRadius);
        }
    }
}