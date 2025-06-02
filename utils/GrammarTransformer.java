package utils;

import parser.Grammar;
import java.util.*;

/**
 * Класс GrammarTransformer нормализует контекстно-свободную грамматику:
 * 1) Удаляет негенерирующие и недостижимые символы
 * 2) Удаляет ε-правила, добавляя все комбинации без nullable-символов
 * 3) Заменяет все «смешанные» и «двух-терминальные» правые части:
 *    – для каждого терминала b вводится новый нетерминал Nb и правило Nb → b
 *    – в исходном правиле терминал b заменяется на Nb
 */
public class GrammarTransformer {

    public static Grammar transformGrammar(Grammar inputGrammar) {
        // 1) Копируем множества нетерминалов, терминалов и стартовый символ
        Set<String> VN = new HashSet<>(inputGrammar.getNonTerminals());
        Set<String> VT = new HashSet<>(inputGrammar.getTerminals());
        String S = inputGrammar.getStartSymbol();

        // 2) Копируем правила в изменяемую структуру P
        Map<String, List<List<String>>> P = new LinkedHashMap<>();
        for (var entry : inputGrammar.getProductions().entrySet()) {
            List<List<String>> clones = new ArrayList<>();
            for (List<String> rhs : entry.getValue()) {
                clones.add(new ArrayList<>(rhs));
            }
            P.put(entry.getKey(), clones);
        }

        // 3) Находим генерирующие нетерминалы
        Set<String> generating = new HashSet<>();
        boolean changed;
        do {
            changed = false;
            for (var lhs : P.keySet()) {
                for (var rhs : P.get(lhs)) {
                    // Если rhs == [ε] ИЛИ все символы в rhs уже в VT или в generating
                    if ((rhs.size() == 1 && rhs.get(0).equals("ε")) ||
                            rhs.stream().allMatch(sym -> VT.contains(sym) || generating.contains(sym))) {
                        if (generating.add(lhs)) {
                            changed = true;
                        }
                    }
                }
            }
        } while (changed);

        // Если стартовый символ не генерирующий – язык пуст
        if (!generating.contains(S)) {
            System.out.println("Язык грамматики пуст");
            return new Grammar(Collections.emptySet(), Collections.emptySet(), S, Collections.emptyMap());
        }

        // 4) Удаляем все негенерирующие нетерминалы и правила с ними
        Set<String> VN1 = new HashSet<>(VN);
        VN1.retainAll(generating);
        P.keySet().removeIf(nt -> !VN1.contains(nt));
        for (var list : P.values()) {
            list.removeIf(rhs -> rhs.stream().anyMatch(sym -> VN.contains(sym) && !generating.contains(sym)));
        }

        // 5) Удаляем недостижимые символы
        Set<String> reachable = new HashSet<>();
        Queue<String> queue = new LinkedList<>();
        reachable.add(S);
        queue.add(S);
        while (!queue.isEmpty()) {
            String cur = queue.poll();
            for (var rhs : P.getOrDefault(cur, Collections.emptyList())) {
                for (String sym : rhs) {
                    if ((VN.contains(sym) || VT.contains(sym))
                            && reachable.add(sym)
                            && VN.contains(sym)) {
                        queue.add(sym);
                    }
                }
            }
        }
        VN1.retainAll(reachable);
        VT.retainAll(reachable);
        P.keySet().removeIf(nt -> !VN1.contains(nt));
        for (var list : P.values()) {
            list.removeIf(rhs -> rhs.stream().anyMatch(sym -> (VN.contains(sym) || VT.contains(sym)) && !reachable.contains(sym)));
        }

        // 6) Удаляем ε-продукции (генерируем все комбинации без nullable)
        Set<String> nullable = new HashSet<>();
        for (var lhs : P.keySet()) {
            for (var rhs : P.get(lhs)) {
                if (rhs.size() == 1 && rhs.get(0).equals("ε")) {
                    nullable.add(lhs);
                    break;
                }
            }
        }

        Map<String, List<List<String>>> newRules = new LinkedHashMap<>();
        for (String lhs : P.keySet()) {
            Set<List<String>> newRHSSet = new LinkedHashSet<>();
            for (List<String> rhs : P.get(lhs)) {
                if (rhs.size() == 1 && rhs.get(0).equals("ε")) {
                    continue;
                }
                newRHSSet.add(new ArrayList<>(rhs));

                // Собираем позиции nullable-символов
                List<Integer> pos = new ArrayList<>();
                for (int i = 0; i < rhs.size(); i++) {
                    if (nullable.contains(rhs.get(i))) {
                        pos.add(i);
                    }
                }
                int combos = 1 << pos.size();
                for (int mask = 1; mask < combos; mask++) {
                    List<String> candidate = new ArrayList<>();
                    for (int i = 0; i < rhs.size(); i++) {
                        if (pos.contains(i)) {
                            int bit = pos.indexOf(i);
                            if ((mask & (1 << bit)) == 0) {
                                candidate.add(rhs.get(i));
                            }
                        } else {
                            candidate.add(rhs.get(i));
                        }
                    }
                    if (!candidate.isEmpty()) {
                        newRHSSet.add(candidate);
                    }
                }
            }
            newRules.put(lhs, new ArrayList<>(newRHSSet));
        }

        // 7) Заменяем «смешанные» и «двух-терминальные» правые части
        //    – создаём новые нетерминалы для терминалов и правила Nb → b
        Map<String, String> termToNewNT = new HashMap<>();
        int newNTcounter = 0;

        Map<String, List<List<String>>> filteredRules = new LinkedHashMap<>();
        for (String lhs : newRules.keySet()) {
            List<List<String>> filteredRHSList = new ArrayList<>();

            for (List<String> rhs : newRules.get(lhs)) {
                // 7.1) Если длина > 2 → удаляем
                if (rhs.size() > 2) {
                    continue;
                }

                // 7.2) Проверяем, есть ли в rhs терминалы и нетерминалы
                boolean hasTerminal = false;
                boolean hasNonTerminal = false;
                for (String sym : rhs) {
                    if (VT.contains(sym))     hasTerminal = true;
                    if (VN1.contains(sym))    hasNonTerminal = true;
                }

                // 7.3) Если правая часть «смешанная» (1+ терминал + хоть 1 нетерминал)
                //       ИЛИ «два терминала»
                if ((hasTerminal && hasNonTerminal) ||
                        (rhs.size() == 2 && hasTerminal && !hasNonTerminal)) {

                    // Будем собирать новую правую часть из нетерминалов
                    List<String> transformedRHS = new ArrayList<>();

                    for (String sym : rhs) {
                        if (VT.contains(sym)) {
                            String newNT = termToNewNT.get(sym);
                            if (newNT == null) {
                                newNT = "N" + sym;
                                // Проверяем уникальность имени newNT
                                while (VN1.contains(newNT) || VT.contains(newNT) || termToNewNT.containsValue(newNT)) {
                                    newNT = "N" + sym + "_" + (newNTcounter++);
                                }
                                termToNewNT.put(sym, newNT);
                                VN1.add(newNT); // добавляем новый нетерминал

                                // Создаём правило newNT → sym
                                filteredRules
                                        .computeIfAbsent(newNT, k -> new ArrayList<>())
                                        .add(Collections.singletonList(sym));
                            }
                            transformedRHS.add(newNT);
                        } else {
                            // Если это старый нетерминал — оставляем без изменений
                            transformedRHS.add(sym);
                        }
                    }

                    // Добавляем преобразованную правую часть
                    filteredRHSList.add(transformedRHS);
                }
                // 7.4) Иначе (оба нетерминала или одиночный символ) — сохраняем как есть
                else {
                    filteredRHSList.add(new ArrayList<>(rhs));
                }
            }

            // 7.5) Если после фильтрации остались правые части — добавляем в итог
            if (!filteredRHSList.isEmpty()) {
                filteredRules.put(lhs, filteredRHSList);
            }
        }

        // 8) Собираем и выводим результирующую грамматику
        Grammar result = new Grammar(VN1, VT, S, filteredRules);
        System.out.println("Нормализованная грамматика (терминалы обёрнуты в новые нетерминалы):");
        System.out.println(result);
        return result;
    }

    // Для тестирования прямо из этого файла
    public static void main(String[] args) {
        Grammar base = Grammar.exampleGrammar();
        System.out.println("Исходная грамматика");
        System.out.println(base);
        transformGrammar(base);
    }
}
