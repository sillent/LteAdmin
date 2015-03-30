package ru.megafon.lte;

import javax.servlet.http.HttpServlet;

/**
 * Created by santa on 30.03.15.
 */
public interface Action {
    public enum ActType {
        ADD,
        DEL,
        CHANGE
    }

    void doAction(ActType typeAction, String... arg);

}
