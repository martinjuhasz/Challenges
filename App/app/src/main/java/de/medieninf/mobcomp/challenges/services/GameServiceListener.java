package de.medieninf.mobcomp.challenges.services;

/**
 * Created by Martin Juhasz on 09/06/15.
 */
public interface GameServiceListener {

    public void userRegistrationUpdated(boolean successfully);

    public void gamesUpdated(boolean successfully);

    public void ratingSent(boolean successfully);

}
