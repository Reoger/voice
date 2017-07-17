// IBookManager.aidl
package reoger.hut.voice;

// Declare any non-default types here with import statements
import reoger.hut.voice.Book;

interface IBookManager {
    List<Book> getBookList();
    void addBook(in Book book);
}
