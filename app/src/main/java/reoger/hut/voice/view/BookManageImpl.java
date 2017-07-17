package reoger.hut.voice.view;

import android.os.Binder;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.RemoteException;

import java.util.List;

import reoger.hut.voice.Book;

/**
 * 模拟AIDL实现
 * Created by 24540 on 2017/6/22.
 */

public class BookManageImpl extends Binder implements IBookManager {

    public BookManageImpl() {
        this.attachInterface((IInterface) this, DESCRIPTOR);
    }

    public static IBookManager asInterface(IBinder obj) {
        if ((obj == null)) {
            return null;
        }
        IInterface iin = obj.queryLocalInterface(DESCRIPTOR);

        if (iin != null && (iin instanceof IBookManager)) {
            return (IBookManager) iin;
        } else {
            return new BookManageImpl.Proxy(obj);
        }
    }


    @Override
    public List<Book> getBookList() throws RemoteException {
        return null;
    }

    @Override
    public void addBook(Book book) throws RemoteException {

    }


    @Override
    public IBinder asBinder() {
        return this;
    }

    @Override
    protected boolean onTransact(int code, Parcel data, Parcel reply, int flags) throws RemoteException {
        switch (code) {
            case INTERFACE_TRANSACTION:
                reply.writeString(DESCRIPTOR);
                return true;
            case TRANSACTION_addBook: {
               data.enforceInterface(DESCRIPTOR);
                Book book;
                if( 0!= data.readInt()){
                    book = Book.CREATOR.createFromParcel(data);
                }else{
                    book = null;
                }
                this.addBook(book);
                reply.writeNoException();
                return true;
            }
            case TRANSACTION_getBookList: {
                data.enforceInterface(DESCRIPTOR);
                List<Book> result = this.getBookList();
                reply.writeNoException();
                reply.writeTypedList(result);
                return true;
            }
        }
        return super.onTransact(code, data, reply, flags);
    }

    /**
     * 代理类
     * 用于调用远程访问
     */
    private static class Proxy implements IBookManager {
        private IBinder mRemote;

        public Proxy(IBinder remote) {
            mRemote = remote;
        }

        public String getInterfaceDescriptor() {
            return DESCRIPTOR;
        }


        @Override
        public List<Book> getBookList() throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            List<Book> result = null;

            try {
                data.writeInterfaceToken(DESCRIPTOR);
                mRemote.transact(TRANSACTION_getBookList, data, reply, 0);
                reply.readException();
                result = reply.createTypedArrayList(Book.CREATOR);
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }


            return result;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            try {
                data.writeInterfaceToken(DESCRIPTOR);
                if (book != null) {
                    data.writeInt(1);
                    book.writeToParcel(data, 0);
                } else {
                    data.writeInt(0);
                }
                mRemote.transact(TRANSACTION_addBook, data, reply, 0);
                reply.readException();
            } catch (RemoteException e) {
                e.printStackTrace();
            } finally {
                reply.recycle();
                data.recycle();
            }
        }

        @Override
        public IBinder asBinder() {
            return null;
        }
    }


}
