package utils;

import java.util.*;

// Класс GrammarTransformer содержит логику нормализации контекстно-свободной грамматики
public class GrammarTransformer {

    public static class Grammar {
        public Set<String> VN = new HashSet<>();
        public Set<String> VT = new HashSet<>();
        public String startSymbol;
        public Map<String, List<String>> rules = new LinkedHashMap<>();

        public Grammar(Set<String> vn, Set<String> vt, String start, Map<String, List<String>> rules) {
            this.VN.addAll(vn);
            this.VT.addAll(vt);
            this.startSymbol = start;
            this.rules.putAll(rules);
        }

        public void printGrammar() {
            System.out.println("VN = " + VN);
            System.out.println("VT = " + VT);
            System.out.println("Начальный символ = " + startSymbol);
            System.out.println("Правила:");
            for (String lhs : rules.keySet()) {
                for (String rhs : rules.get(lhs)) {
                    System.out.println("  " + lhs + " → " + rhs);
                }
            }
        }
    }

    public static Grammar transformGrammar() {
        // Исходные множества нетерминалов, терминалов и стартового символа
        Set<String> VN = new HashSet<>(Arrays.asList("S", "L", "M", "P", "N"));
        Set<String> VT = new HashSet<>(Arrays.asList("n", "m", "l", "p", "@", "⊥"));
        String S = "S";

        // Исходные правила грамматики
        Map<String, List<String>> P = new LinkedHashMap<>();
        P.put("S", new ArrayList<>(Arrays.asList("@nL", "@mM", "P")));
        P.put("L", new ArrayList<>(Arrays.asList("M", "Ll⊥", "Lm⊥", "ε")));
        P.put("M", new ArrayList<>(Arrays.asList("L", "Mm", "mm")));
        P.put("N", new ArrayList<>(Arrays.asList("pN@", "@")));
        P.put("P", new ArrayList<>(Arrays.asList("nmP")));

        // Проверка существования языка
        Set<String> generating = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            for (String lhs : P.keySet()) {
                for (String rhs : P.get(lhs)) {
                    // Правило генерационно (т.е. порождает минимум одну цепочку из терминалов), если rhs — ε или все символы rhs — терминалы или уже известные генераторы
                    if (rhs.equals("ε") || rhs.chars().allMatch(c -> VT.contains(String.valueOf((char) c)) || generating.contains(String.valueOf((char) c)))) {
                        if (generating.add(lhs)) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        // Если стартовый символ не генератор, то язык пуст
        if (!generating.contains(S)) {
            throw new RuntimeException("Язык грамматики пуст");
        }

        // Удаление бесполезных символов
        Set<String> VN1 = new HashSet<>(VN);
        VN1.retainAll(generating); // Оставляем только генераторы
        for (String key : new ArrayList<>(P.keySet())) {
            // Удаляем правые части, содержащие неггенерирующие нетерминалы
            P.get(key).removeIf(rhs -> Arrays.stream(rhs.split("")).anyMatch(sym -> VN.contains(sym) && !generating.contains(sym)));
        }

        // Удаление недостижимых символов
        Set<String> reachable = new HashSet<>();
        reachable.add(S); // Стартовый символ достижим
        Queue<String> queue = new LinkedList<>();
        queue.add(S);

        // BFS по правилам для определения достижимых символов
        while (!queue.isEmpty()) {
            String current = queue.poll();
            for (String rhs : P.getOrDefault(current, Collections.emptyList())) {
                for (String sym : rhs.split("")) {
                    if ((VN.contains(sym) || VT.contains(sym)) && !reachable.contains(sym)) {
                        reachable.add(sym);
                        queue.add(sym);
                    }
                }
            }
        }

        // Оставляем только достижимые символы
        VN1.retainAll(reachable);
        VT.retainAll(reachable);
        P.keySet().retainAll(VN1);
        for (String key : new ArrayList<>(P.keySet())) {
            // Удаляем правые части, содержащие недостижимые символы
            P.get(key).removeIf(rhs -> Arrays.stream(rhs.split("")).anyMatch(sym -> (VN.contains(sym) || VT.contains(sym)) && !reachable.contains(sym)));
        }

        // Удаление ε-правил
        Set<String> nullable = new HashSet<>();
        // Определяем nullable нетерминалы (порождающие ε)
        for (String lhs : P.keySet()) {
            for (String rhs : P.get(lhs)) {
                if (rhs.equals("ε")) {
                    nullable.add(lhs);
                }
            }
        }

        // Создаем новые правила, исключая ε, но с добавлением всех вариантов без nullable символов
        Map<String, List<String>> newRules = new LinkedHashMap<>();
        for (String lhs : P.keySet()) {
            Set<String> newRHS = new HashSet<>();
            for (String rhs : P.get(lhs)) {
                if (rhs.equals("ε")) continue; // пропускаем ε, оно будет учтено косвенно
                newRHS.add(rhs);

                // Ищем позиции nullable символов в правой части
                List<Integer> nullablePositions = new ArrayList<>();
                String[] symbols = rhs.split("");
                for (int i = 0; i < symbols.length; i++) {
                    if (nullable.contains(symbols[i])) {
                        nullablePositions.add(i);
                    }
                }

                // Перебираем все комбинации исключения nullable символов
                int combinations = 1 << nullablePositions.size();
                for (int mask = 1; mask < combinations; mask++) {
                    StringBuilder newStr = new StringBuilder();
                    for (int i = 0; i < symbols.length; i++) {
                        if (nullablePositions.contains(i)) {
                            int bitIndex = nullablePositions.indexOf(i);
                            // Если бит равен 0 — исключаем символ, иначе оставляем
                            if ((mask & (1 << bitIndex)) == 0) {
                                newStr.append(symbols[i]);
                            }
                        } else {
                            newStr.append(symbols[i]);
                        }
                    }
                    if (!newStr.toString().isEmpty()) {
                        newRHS.add(newStr.toString());
                    }
                }
            }
            newRules.put(lhs, new ArrayList<>(newRHS));
        }

        // Возвращаем нормализованную грамматику
        return new Grammar(VN1, VT, S, newRules);
    }

    public static void main(String[] args) {
        Grammar g = transformGrammar();
        g.printGrammar();
    }
}
