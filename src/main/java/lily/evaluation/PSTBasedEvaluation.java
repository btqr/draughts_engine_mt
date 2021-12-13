package lily.evaluation;

import lily.engine.Color;
import lily.engine.Position;
import lily.io.NotationTranslator;

import static lily.utils.BoardUtils.*;

public class PSTBasedEvaluation implements WeightedEvaluation {

    private double[] weights = new double[]{7.76075705596768, -0.05359405928726402, 11.373411192074627, 4.79325984701221, -7.13185906695012, -6.8046004912539795, -2.268323181570268, 9.911319688785987, 3.7899199008841387, -5.2550271988449415, -1.670480145690013, 7.742897650902807, 8.692296587963845, -0.594956330603833, -4.4984086484196535, 3.614460403321011, 2.0985100583734515, 1.3547024933232152, 1.8883034692369434, -3.4628706271784404, -4.344003868467299, -2.9739145826066133, 0.03340994053503825, -3.183520512951671, 2.9156124582578986, 0.7172971808565157, -7.010349061005585, -5.3687131160516826, -13.889775795227346, -11.134419032582903, -9.35696075292746, 0.2394339021754217, -7.063678032207758, -14.322344031436815, -11.37221766373728, 2.162036899239759, -0.14435358126461517, -5.076023757513466, -1.3304186171384982, -7.093651288927451, -2.052832719108748, -0.16627896024379685, -0.276180056258402, -1.135096096240813, 29.0035580338392, 3.0263958280946794, 1.2405441966009034, -4.1011646558026955, 1.6290460536092373, -0.17381947971649186, 4.906917557081412, -1.1653711925552916, 5.956758379849692, 2.236718905178478, -7.716999102410569, -3.0883692847115416, 0.6730082380717136, 5.438438091244796, 4.374292299052036, -6.786779352603936, 0.25747925410808514, 5.7040627577326415, 9.55463245868867, -0.6509980021772661, -2.15848560387291, -1.575045244635997, 4.004740560279078, 7.451544035742944, 0.9556699274944409, -3.1882818722574777, -3.9579620710231915, 2.575684180211007, 4.508039025097386, -2.6116004079785498, 4.945205804409713, -1.4208178104264384, -5.712036416963123, -4.5591462380720715, -10.439326174009194, -9.538759798138116, -4.828634217995693, -9.090581908200535, -17.60322777603615, -16.08175095324002, 1.2796295376594764, -3.6253416219807697, -2.7907700772451336, -3.8019505476332003, -19.935675523541217, -13.98402580745127, 24.946819938095132, -7.883788712754121, -8.524358929441316, 0.6962628648403197, 11.574605226028217, 0.5486116680203853, 3.180623305386896, -0.7569505505245272, -0.7529126863958775, -0.19075571698656676, 5.144644864092351, -1.625320541489071, 2.6353364386224643, 2.9116502294957396, -13.11254872261333, -5.296362989789277, -3.5079338721857662, 0.2618503453081437, -1.2806935487366875, -8.82173163417018, -4.988503390324876, 2.630363013960781, 0.05596156077959689, -1.3360589505619331, -4.856655028386919, -4.808308816427012, -0.1085039912560816, 0.8040803065249699, -1.2289189615171392, -8.838692274048187, -5.4509104080747, -1.9614569427980093, -0.0714748477492392, -7.370503425126468, -4.722806177914831, -0.7764881065315655, -4.2431693220094315, -1.5693272604847324, -7.778872528823835, -6.892737575479459, -4.719079228475734, 0.6833776124114423, -2.8489167379471763, -4.718811489229355, -6.619378626321214, -6.980809329631425, 15.890810421651333, 11.947924813259336, 3.3315109279999424, 8.94242803445034, 38.53824139192347, 10.248826015044829, 15.541628155438294, 28.439657619249772, 8.046645887326447, -1.7210994332293346, -1.921667798614064, 1.9280767122657374, -3.271623489898223, 0.8129473911274016, 13.595057631163472, 11.338720871163172, 1.2860050119966933, -4.165092862491444, 19.944288220954913, -9.127614656145235, 0.3108398833719374, -4.089700619240409, -2.0971320323505793, -7.726769161928393, -5.89884164458448, -7.417656547860125, -8.144909783008067, 3.473691945353038, -6.545640492952898, -5.961945915108476, -3.519931071687055, -4.3924601325847, -6.6734651611227225, -12.637633077973886, -20.526681950131735, -3.7577991803929285, -3.6372099421278365, -1.3452531644236037, 3.009853017954806, -6.808698625508797, -7.969190791587997, -3.676858839008446, -3.3086240086433207, -5.469057504612676, -8.682867366296461, -2.94618736186929, 4.189717272929981, 2.6509723599160733, -2.115454406030555, -4.455751242630616, 1.6668831362118548, 2.708843302490771, 13.326898559429392, 7.570104832537597, 30.800728525013135, 3.497543041296204, 20.569932709259053, 28.200951197278386, 9.534180543385997, 2.276123158149537, 2.662260903456713, 0.7637612889352258, 0.3236311446425004, -0.03272828303953355, 1.0184290922936183, 0.6688839694952029, 0.9514763607295788, 1.571918808615853, 1.878970444914523, 4.373979278458064, 3.4917654315216877, 2.7612908952881687, 3.134604999339733, -1.2899784872981956, -1.2947342557799792, 5.758445183964678, 1.8724600982810973, -4.430354835998796, -3.8812392204648476, 2.58737343251669, 5.375335293963788, -1.4461096480362026, -0.9666325469386615, 5.421136928293023, 5.732359233569553, 0.0381008808462659, -0.7883723159572557, 1.4433011085819418, 2.496416027968309, 0.7513088127298637, -0.810609311487702, -1.085143728274255, 3.02483700834801, 2.3753714123608356, 1.389585205654713, 3.193541341968566, 5.866766409113369, 7.647456599750177, 3.624610735526288, 0.4178637015812483, 2.3649977093309467, 4.664657049028629, -1.0083912887413127, -0.41247781609532413, 0.44154602217731886, -1.6150402385159448, -2.2698557319901975, -2.8663323855968987, -3.4188400135476553, -4.692886915552825, -5.57043981765169, -5.8428939388228365, -9.100866090948097, -7.3153119405542375, -10.26986948069311, -14.90649744468898, -14.999406879505782, -20.932804229230445, -8.214056254517516, -10.398389198556796, -3.8769192298218993, -5.014842885657279, 0.04339742710012451, -0.2518963727861946, -1.6468402294836257, -2.8781952234522734, -5.662807788387125, -4.879466195851848, -4.224125958584895, -5.516474304727757, -4.592985083280735, 2.926644448630042, 3.571624036160622, 2.618071142103729, -9.163881024541935, -15.33737925328836, 6.234872012227742, 0.4661606107254742, -15.066560023149703, 2.2563311283215124, -2.4075080449912414, -3.7737988679680017, -7.8193282889053375, -2.004546348441871, -6.867481514203125, -1.8078940829147336, 0.2167606443977399, 0.8207890691435566, 2.9053109474551047, 2.061845888633719, 12.103183092172245, -1.1109712050256106, -2.6001164777617634, 7.447090102116974, -4.0493003335768325, 5.801169762852292, -5.564021932336602, -4.908955755694542, 2.9828452859649643, -4.0546427206776325, 8.534777491502505, 1.2927865854742064, -1.677001780417412, 0.09057012157637634, 8.94176019525059, 2.9196722887773654, 3.0489040221939874, -0.76101208697594, -2.4511726604944744, -2.757405016411923, -0.5469266486232932, -2.909369495954375, -1.0608499307836745, -2.655116348271302, 5.005092315197777, 0.29415019448386115, -3.411801573564692, -4.641630047798182, -6.240182953285123, -3.905370623428902, -2.803347958214089, -12.335915368252943, -15.33478280683609, -7.071970450961682, -3.5731323543007156, 1.736706848079379, 1.2014452728174658, -0.13929329053027392, 4.975607658139262, 4.436786152301539, 2.1456294361794144, 4.857485727242758, 4.339088209071584, 2.6340296508293664, 7.780332808965398, 11.899757164305237, 15.84821360449052, 18.32066552621743, 22.182050640505583, 9.507704137304257, 3.5675499888445734, 5.523128335293941, 0.7800624632582052, 7.269323100085613, 6.331137920085952, 3.571730796570125, 0.32672576837362033, -4.482756773672282, 1.0248335823331784, 2.428054157112708, 4.376177157896175, 8.44666667240235, 16.966207794397246, 23.380918967504666, 18.961010516813655, 14.395893658614082, 5.577611398140719, 19.62015930335699, 2.1733334398583515, 9.465405185574191, -0.147328937962671, -16.746197413226227, 6.613854676798407, 9.716325097349788, -6.446177394943595, -2.9135095903904062, 5.479170933887277, -0.40573026106204063, 2.3579734374620456, -1.7322844552859686, -1.740359856778463, 1.3047011526157615, -4.1305441876724664, -0.3538513198039712, 2.2618492140183304, 13.412854921331526, -3.034225959332141, -3.1218015768641902, 14.153481294746943, -1.2678815770550393, 2.6320356826989215, 23.491394541960553, 25.997263806233615, 9.61478804659606};
    private static final NotationTranslator notationTranslator = new NotationTranslator();

    @Override
    public double evaluate(Position position, Color color) {
        double score = getScore(position);
        return color == Color.WHITE ? score : -score;
    }

    public double getScore(Position position) {
        int phase = getPhase(position);
        double pstScore = countPST2(position, phase);
        double pawnScore = position.getNumberOfWhite() * 1000 - position.getNumberOfBlack() * 1000 + position.getNumberOfWhiteKings() * 2000 - position.getNumberOfBlackKings() * 2000;
        double tempoDiff = countTempo(position.getTempoDiff(), phase);
        double balanceDiff = countBalance(position.getBalanceDiff(), phase);

        double score = pawnScore + balanceDiff + pstScore;
        if (position.getNumberOfWhite() != position.getNumberOfBlack()) {
            return score;
        } else {
            return score + tempoDiff;
        }
    }

    private double countPST2(Position position, int phase) {
        int offset = 0;
        if (phase == 2) offset = 50;
        if (phase == 3) offset = 100;
        if (phase == 4) offset = 150;
        double score = 0;
        long fields = position.getWhiteFields() | position.getBlackFields();
        int[] board = position.getBoard();
        while (Long.numberOfTrailingZeros(fields) != 64L) {
            int field = Long.numberOfTrailingZeros(fields);
            int applicationField = notationTranslator.toApplicationField(field);
            if (board[applicationField] == WHITE_PAWN || board[applicationField] == WHITE_KING) {
                score += weights[offset + 50 - field];
            }
            if (board[applicationField] == BLACK_PAWN || board[applicationField] == BLACK_KING) {
                score -= weights[offset + field - 1];
            }
            fields &= ~(1L << field);
        }
        return score;
    }

    private int getPhase(Position position) {
        int numberOfPawns = position.getNumberOfBlack() + position.getNumberOfWhite();
        if (numberOfPawns > 30) return 1;
        if (numberOfPawns > 20) return 2;
        if (numberOfPawns > 10) return 3;
        else return 4;
    }

    private double countBalance(int balance, int phase) {
        int offset = 200;
        if (phase == 2) offset = 220;
        if (phase == 3) offset = 240;
        if (phase == 4) offset = 260;
        double score = weights[offset + Math.min(Math.abs(balance), 19)];
        if (balance > 0) {
            return score;
        } else if (balance < 0) {
            return -score;
        } else {
            return 0;
        }
    }

    private double countTempo(int tempoDiff, int phase) {
        int offset = 280;
        if (phase == 2) offset = 300;
        if (phase == 3) offset = 320;
        if (phase == 4) offset = 340;
        double score = weights[offset + Math.min(Math.abs(tempoDiff), 19)];
        if (tempoDiff > 0) {
            return score;
        } else if (tempoDiff < 0) {
            return -score;
        } else {
            return 0;
        }
    }

    @Override
    public void setWeights(double[] weights) {
        this.weights = weights;
    }

    @Override
    public double[] getWeights() {
        return weights;
    }
}