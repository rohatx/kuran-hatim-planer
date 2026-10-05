package ry.dev.kuranhatimplanner;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import java.io.File;

public class PdfProvider extends ContentProvider {
    @Override public boolean onCreate(){return true;}
    private File file(Uri uri){return new File(getContext().getCacheDir(), Uri.decode(uri.getLastPathSegment()));}
    @Override public String getType(Uri uri){return "application/pdf";}
    @Override public ParcelFileDescriptor openFile(Uri uri,String mode)throws java.io.FileNotFoundException{return ParcelFileDescriptor.open(file(uri),ParcelFileDescriptor.MODE_READ_ONLY);}
    @Override public Cursor query(Uri uri,String[] projection,String selection,String[] args,String sort){File f=file(uri);MatrixCursor c=new MatrixCursor(new String[]{"_display_name","_size"});c.addRow(new Object[]{f.getName(),f.length()});return c;}
    @Override public int delete(Uri uri,String s,String[] a){File f=file(uri);return f.delete()?1:0;}
    @Override public Uri insert(Uri uri,ContentValues v){throw new UnsupportedOperationException();}
    @Override public int update(Uri uri,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
}
