package exam.bitbyte;


public class YorkData
{
	private String title;
	private int image;
    private boolean checked;
	public YorkData()
	{
		super();
		
	}

	public YorkData(String title, int image)
	{
		super();
		this.title = title;
		this.image = image;
	}
	public boolean getchecked()
	{
		return this.checked;
	}
	public void setchceck()
	{
		this.checked=!this.checked;
	}
	public String getTitle()
	{
		return title;
	}
	public void setTitle(String title)
	{
		this.title = title;
	}

	public int getImage()
	{
		return image;
	}

	public void setImage(int image)
	{
		this.image = image;
	}
}
