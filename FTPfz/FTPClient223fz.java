package FTPfz;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.Arrays;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

//enum ��������� ������� Singleton
public enum FTPClient223fz implements FTPClientFZ
{
	INSTANCE;
	public static FTPClient223fz getInstance() { return INSTANCE; }
	
	private static FTPClient ftp = new FTPClient();
	public final static String SERVER = "ftp.zakupki.gov.ru";
	public final static String USER = "fz223free";
	public final static String PASSWD = "fz223free";
	private final static String basicWorkspace = "/out/published";
	private final static String[] parsingFolders = {"contract", "contractInfo", 
			"contractCompleting", "purchaseNotice", "purchaseNoticeAE", "purchaseNoticeAE94", 
			"purchaseNoticeAESMBO", "purchaseNoticeEP", "purchaseNoticeIS", "purchaseNoticeOA",
			"purchaseNoticeOK", "purchaseNoticeZK", "purchaseNoticeZKESMBO", "purchaseProtocol",
			"purchaseProtocolZK", "purchaseProtocolVK", "purchaseProtocolPAEP", "purchaseProtocolPAAE",
			"purchaseProtocolPAAE94", "purchaseProtocolOSZ", "purchaseProtocolRZOK", 
			"purchaseProtocolRZ1AE", "purchaseProtocolRZ2AE"};
	private final static String downloadPath = "D:/Zakupki";
	
	@Override
	public void connect() throws SocketException, IOException 
	{
		ftp.connect(SERVER);
		System.out.println("SERVER: " + ftp.getReplyStrings());
	}

	@Override
	public void login() throws IOException 
	{
		if(ftp.login(USER, PASSWD))
		{
			System.out.println("LOGGED IN SERVER");
			ftp.enterLocalPassiveMode(); //��������� ����� ������� � ��������
		}
		else
		{
			System.out.println("Failed to log into the server");
            return;
		}
	}
	
	@Override
	public void parseFTPServer() throws IOException
	{
		searchRegionsDirectories(basicWorkspace);
	}
	
	private void searchRegionsDirectories(String workspace) throws IOException
	{
		
		System.out.println(workspace); //DEBUG
		FTPFile[] namesDirectories = ftp.listDirectories(workspace);
		for(FTPFile n: namesDirectories)
		{
			if(!n.getName().equals("archive"))
			{
				makeDownloadDirectories(workspace + "/" + n.getName());
				searchInRegions(workspace + "/" + n.getName());
			}
		}
	}
	
	//������� ���������� �� ��������� ����� ��� �������� ������
	private void makeDownloadDirectories(String workspace)
	{
		 for(String s: parsingFolders)
		 {
			 File dir = new File(downloadPath + workspace + "/" + s + "/daily/unzip");
			 dir.mkdirs();
			 dir = new File(downloadPath + workspace + "/" + s + "/unzip");
			 dir.mkdir();
		 }
	}
	
	//����� ����������� ���������� �� �������
	private void searchInRegions(String workspace) throws IOException
	{
		FTPFile[] namesDirectories = ftp.listDirectories(workspace);
		for(FTPFile n: namesDirectories)
		{
			if(Arrays.asList(parsingFolders).contains(n.getName()))
			{
				searchFiles(workspace + "/" + n.getName());
				searchFiles(workspace + "/" + n.getName() + "/daily");
			}
		}
	}
	
	//����� ����� �� �������
	private void searchFiles(String workspace) throws IOException
	{
		System.out.println(workspace); //DEBUG
		FTPFile[] files = ftp.listFiles(workspace);
		for(FTPFile remote: files)
		{
			if(remote.isFile())
			{
				if(downloadFile(downloadPath + workspace + "/" +remote.getName(),
						workspace + "/" +remote.getName()))
				{
					unzipFile(downloadPath + workspace + "/" +remote.getName(),
							downloadPath + workspace);
				}
				else
				{
					System.out.println("�� ������� ��������� " + workspace + "/" +remote.getName());
				}
			}
		}
	}
	
	//��������� �����
	private boolean downloadFile(String localPath, String remotePath) throws IOException
	{
		File localFile = new File(localPath);
		localFile.createNewFile();
		OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(localFile));
		if(!ftp.retrieveFile(remotePath, outputStream))
		{
			outputStream.close();
			return false;
		}
		outputStream.close();
		return true;
		
	}
	
	//��������������� ����
	private void unzipFile(String filePath, String path)
	{
		try(ZipInputStream zin = new ZipInputStream(new FileInputStream(filePath)))
        {
            ZipEntry entry;
            String name;
            while((entry = zin.getNextEntry()) != null)
            {
                  
                name = entry.getName();
                File localFile = new File(path + "/unzip/" + name);
                localFile.createNewFile();

                FileOutputStream fout = new FileOutputStream(localFile);
                byte[] buffer = new byte[4096];
                for (int len = zin.read(buffer); len != -1; len = zin.read(buffer)) 
                {
                    fout.write(buffer, 0, len);
                }
                
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
        }
        catch(Exception ex)
		{
            System.out.println(ex.getMessage());
        } 
	}
}
