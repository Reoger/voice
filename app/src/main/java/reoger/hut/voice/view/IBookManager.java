package reoger.hut.voice.view;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

import java.util.List;

import reoger.hut.voice.Book;

/**
 * Created by 24540 on 2017/6/22.
 */

public interface IBookManager extends IInterface{

    static final String DESCRIPTOR = "reoger.hut.voice.view";
    static final int TRANSACTION_getBookList = IBinder.FIRST_CALL_TRANSACTION + 0;
    static final int TRANSACTION_addBook = IBinder.FIRST_CALL_TRANSACTION + 1;
    public List<Book> getBookList() throws RemoteException;
    public void addBook(Book book) throws RemoteException;


}
