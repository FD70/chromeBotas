package ru.fd70.funcs;

import java.util.Arrays;

public class SeGenerators {
    public static String makeRandomINN(int len) {
        int[] generated = new int[10];
        StringBuilder output = new StringBuilder();
        int[] n2_12 = new int[] {7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
        int[] n1_12 = new int[] {3, 7, 2, 4, 10, 3, 5, 9, 4, 6, 8};
        int[] n1_10 = new int[] {2, 4, 10, 3, 5, 9, 4, 6, 8};
        for (int i = 0; i < 9; i++) {
            generated[i] = (int) (Math.random() * 10);
        }
        if (len == 12) {
            generated = Arrays.copyOf(generated, 12);
            generated[9] = (int) (Math.random() * 10);
            int n2 = 0;
            int n1 = 0;
            for (int g = 0; g < n2_12.length; g++)  {
                n2 += n2_12[g] * generated[g] ;
            }
            generated[10] = n2 % 11 % 10;
            for (int g = 0; g < n1_12.length; g++)  {
                n1 += n1_12[g] * generated[g];
            }
            generated[11] = n1  % 11 % 10;
        } else {
            int n1 = 0;
            for (int g = 0; g < n1_10.length; g++) {
                n1 += n1_10[g] * generated[g];
            }
            generated[9] = n1  % 11 % 10;
        }
        for (int h:generated) {
            output.append(h);
        }
        return output.toString();
    }
    public static String makeRandomOGRN() {
        int[] generated = new int[12];
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < 12; i++) {
            generated[i] = (int) (Math.random() * 10);
        }
        for (int h:generated) {
            output.append(h);
        }
        int cNum = (int) (Long.parseLong(output.toString()) % 11 % 10);
        output.append(cNum);
        return output.toString();
    }
    public static String makeRandomOGRNIP() {
        int[] generated = new int[14];
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < 14; i++) {
            generated[i] = (int) (Math.random() * 10);
        }
        for (int h:generated) {
            output.append(h);
        }
        int cNum = (int) (Long.parseLong(output.toString()) % 13 % 10);
        output.append(cNum);
        return output.toString();
    }
    public static String makeRandomSNILS() {
        int[] generated = new int[9];
        int cNum = 0;
        StringBuilder output = new StringBuilder();
        for (int i = 0; i < 9; i++) {
            generated[i] = (int) (Math.random() * 10);
            cNum += (9-i) * generated[i];
        }
        for (int h:generated) {
            output.append(h);
        }
        cNum = cNum % 101;
        if (cNum == 100) {
            output.append("00");
        } else if (String.valueOf(cNum).length() == 2) {
            output.append(cNum);
        } else {
            output.append("0").append(cNum);
        }
        return output.toString();
    }

    public static String getRandomLastName() {
        String output;
        //String[] names = new String[] {"Савичев", "Талашин", "Узков", "Фарберов", "Хандошкин", "Цибесов"};
        String[] names = new String[] {"Настырный", "Жестокий", "Заносчивый", "Нелюдимый", "Закрытый", "Вредный",
                "Вредина", "Биполярный", "Упрямый", "Высокомерный", "Неприветливый", "Пугливый", "Настораживающий",
                "Брезгливый", "Придирчивый", "Дерзкий", "Мрачный", "Угрожающий", "Пугающий", "Обидчивый", "Необщительный",
                "Агрессивный", "Стеснительный", "Злорадный", "Нетерпимый", "Бешенный", "Раздражительный", "Молчаливый"};
        output = names[(int) (Math.random() * names.length)];
        return output;
    }
    public static String getRandomFirstName() {
        return getRandomFirstName(0);
    }
    public static String getRandomFirstName(int gender) {
        String output;
        if (gender == 0) {
            String[] names = new String[] {"Артур", "Богдан", "Вольдемар", "Григорий", "Дональт"};
            output = names[(int) (Math.random() * names.length)];
        } else {
            String[] names = new String[] {"Адель", "Берта", "Веста", "Гертруда", "Диана"};
            output = names[(int) (Math.random() * names.length)];
        }
        return output;
    }
}