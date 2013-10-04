package common;

import java.rmi.RemoteException;

public interface PrimaryServerStub extends ServerApi {
    Reply joinGame() throws RemoteException;
}
