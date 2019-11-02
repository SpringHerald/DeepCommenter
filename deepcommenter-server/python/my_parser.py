import javalang

STR = "STR_"
NUM = "NUM_"
BOOL = "BOOL_"


def camel_split(word='entrySize'):
    words = []
    tmp = ''
    for i in range(len(word)):
        if i == 0:
            tmp += word[i]
        else:
            if word[i].islower():
                tmp += word[i]
            elif tmp[-1].isupper():
                if i < len(word) - 1 and word[i + 1].isupper() or i == len(word) - 1:
                    tmp += word[i]
                else:
                    words.append(tmp)
                    tmp = word[i]
            else:
                words.append(tmp)
                tmp = word[i]

    words.append(tmp)
    return words


def parse_java(code):
    code_original = code
    tokens = list(javalang.tokenizer.tokenize(code))
    tks = []
    camel_tks = []

    for tk in tokens:
        if tk.__class__.__name__ == 'Identifier':
            camel_tks.extend(camel_split(tk.value))
            tks.append(tk.value)
        elif tk.__class__.__name__ == 'String' or tk.__class__.__name__ == 'Character':
            tks.append(STR)
            camel_tks.append(STR)
        elif 'Integer' in tk.__class__.__name__ or 'FloatingPoint' in tk.__class__.__name__:
            tks.append(NUM)
            camel_tks.append(NUM)
        elif tk.__class__.__name__ == 'Boolean':
            tks.append(BOOL)
            camel_tks.append(BOOL)
        else:
            tks.append(tk.value)
            camel_tks.append(tk.value)
    tks = [t.lower() for t in tks]
    camel_tks = [t.lower() for t in camel_tks]
    code = ' '.join(tks)
    camel_code = ' '.join(camel_tks)
    return code_original, code, camel_code


if __name__ == '__main__':
    s = """
public Map pickUpRequest(String parkingSpaceId, int layer, String areaId, String orderId) { 
 businessOrderService.createPickUpOrder(parkingSpaceId, layer, areaId, 1, Direction.DIR1, orderId, taskType); 
   Map<String, String> resp = new HashMap<>(); 
 resp.put("areaId", areaId); 
 return resp; 
 }
    """
    s = """
    public PaymentDataException(String message, Reason reason){
        super(message);
        setMessageKey(getMessageKey()+reason.toString());
    }
    """
    res1, res2, res3 = parse_java(s)
    print(res2)
    print('###')
    print(res3)
