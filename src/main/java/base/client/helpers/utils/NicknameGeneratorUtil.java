package base.client.helpers.utils;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;

public class NicknameGeneratorUtil {

    public static String generateAPlayer() {
        return ("APlayer" + RandomStringUtils.randomAlphabetic(3) + RandomStringUtils.randomNumeric(2));
    }
    public static String generatenickname() {

        String startname="";

        String [] prefix1={"zxc","Its","Itz","NSC","A","Mr","Skid","Dr","I","The","Mc","Your","Me","Jr","Ezz"};
        String [] postfix1={"MC","YT","TV","Xd","XD","zxc","XXX","XL","Tixic"};

        String [] material1={"Wood","Silver","Gold","Diamond","Bronze","Steel","Rubin","Emerald","Netherite"};

        String [] adjective1={"Bad","Angry","Awful","Bold","Fallen","Fobbiden","Old","Hot","Lucky","Lonely","Great","Noisy",
                "Real","Silent","Poor","Terrible","Tasty","Tired","Absolut",
                "Broken","Cold","Crazy","Cutie","Super","Impossible","Fresh","Meta","Yappy","Funny","Hype",
                "Hilarious","Impossible","Candy","Last","Final","Upset","Big","Special"};


        String [] nicknames1={"Nearelin","Kenynonn","Stotic","Stiankay","Rilareris","Elleio",
                "Ddenomer","Atys","Temi","Menaesolo","Melal","Isare","Siliniden","Ciode","Hyanin","Nanilioty",
                "Brgalis","Wan","Byni","Nuancat","Lelirxe","Llerkepo","Spec","Hazard",
                "Diari","Xolakyn","Bon","Yan","Unetyana","Protei","Sumandoro","Qustironn","Wih","Entr",
                "Welir","Chen","Onnenthi","Pephe","Hoaz","Tanien","Kallen","Rizz",
                "Laulanna","Xenbyret","Baega","Yancholda","Xarm","Qerill","Zadanzo","Dyne","Shava","Sorna",
                "Fredamk","Foxy","Iringto","Bitbri","Mars","Jandesh","Denis",
                "Katorgen","Zhaffirk","Xietta","Woopaste","Oramo","Erie","Zukan","Qabi","Incognito","Cheley",
                "Fanuri","Sill","Rintor","Chori","Haleb","Haram","Ottis",
                "Halal","Daddy","Mommy","Vaha","Fatte","Teran","Liste","Coni","Ureq","Faro",
                "Avis","Avi","Sona","Safia","Saffron","Leon","Slayer","Basl",
                "Johan","Allah","Jorno","Kepler","Jim","Sup","Eclipse","Roze",
                "Ruby","Coco","Anca","Niamh","Biba","Greta","Meryl","Eoin","Edna","Herman",
                "Goodby","Ajay","Rani","Leo","Raj","Nikhil","Gremlin","Kekatilo",
                "Amrit","Ruby","Sunil","Hattie","Maeve","Otis","Remi","Luella","Richy",
                "Isla","Iris","Mabel","Louisa","Diana","Signe","Jorno","Chikatilo",
                "Naomi","Florence","Gemma","Lavender","Waverly","Nadine","Severin","Vernon","Theodore","James",
                "Marion","Adrian","Stefan","Silas","Alec","Otto","Exip","Tusk","Boba",
                "Evan","Decker","Gena","Fletcher","Howard","Cleveland","French","Finch","Donte","Dante",
                "Foster","Jillian","Becker","Sherman","Liliana","Mcbride","Sanya","Popusk",
                "Lewis","Ibarra","Marla","Lozano","Gwen","Contreras","Daron","Shelton","Edward","Lowery",
                "Werner","Esparza","Petersen","Sherman","Dolly","Frye","Spreadl","Volant",
                "Irving","Estrada","Beverley","Gill","Jeff","Santos","Bruno","Calen",
















        };

        SecureRandom secureRand = new SecureRandom();

        boolean wasadj=false;


        int numberrcount=0; for(int i=0;i<4;i++) {if(Math.random()>0.85) {numberrcount++;}  }


        startname=nicknames1[secureRand.nextInt(nicknames1.length)];

        if(Math.random()>0.95) { 	startname=startname.toLowerCase();	}
        else if(Math.random()>0.99) {startname=startname.toUpperCase(); }

        if(Math.random()>0.9) {
            startname=startname+postfix1[secureRand.nextInt(postfix1.length)];
        }


        if(Math.random()>0.9 && !wasadj) { wasadj=true;
            startname=material1[secureRand.nextInt(material1.length)]+startname;
        }
        if(Math.random()>0.8 && !wasadj) { wasadj=true;
            startname=adjective1[secureRand.nextInt(adjective1.length)]+startname;
        }



        if(numberrcount>0) {
            if(Math.random()>0.85 && !wasadj) {
                startname=prefix1[secureRand.nextInt(prefix1.length)]+startname;
            }
            if(Math.random()>0.85) {
                startname="_"+startname+"_";
            }

            startname+=RandomStringUtils.randomNumeric(numberrcount);


        }else {
            if(Math.random()>0.85 && !wasadj) {
                startname=prefix1[secureRand.nextInt(prefix1.length)]+startname;
            }
            if(Math.random()>0.85) { 	startname="_"+startname+"_"; 	}

        }



        if(Math.random()>0.99) { 	startname=startname.toLowerCase();	}
        else if(Math.random()>0.99) {startname=startname.toUpperCase(); }






        return (startname);
    }





}