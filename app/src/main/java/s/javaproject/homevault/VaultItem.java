package s.javaproject.homevault;

public class VaultItem {

    public String _EAN;
    public String _description;
    public String _image;
    public Integer _amount;
    private String _dateTime;
    public Integer _type;

    public VaultItem(String EAN, String description, Integer amount, Integer type)
    {
        this._EAN = EAN;
        this._description = description;
        this._amount = amount;
        this._type = type;
    }

    public String getEAN()
    {
        return this._EAN;
    }

    public String getDescription()
    {
        return this._description;
    }

    public String getImage()
    {
        return this._image;
    }

    public int GetAmount()
    {
        return this._amount;
    }

    public void SetAmount(Integer amount)
    {
        this._amount = amount;
    }

    public String count()
    {
        return this._amount.toString();
    }

    public String getType()
    {
        return this._type.toString();
    }

    public String getDate()
    {
        return this._dateTime;
    }
}
