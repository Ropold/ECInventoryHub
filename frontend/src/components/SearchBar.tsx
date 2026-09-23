import {translatedInfo} from "./utils/TranslatedInfo.ts";
import "./styles/SearchBar.css"

type SearchBarProps = {
    searchQuery: string;
    setSearchQuery: (value: string) => void;
    language: string;
}

export default function SearchBar(props: Readonly<SearchBarProps>) {

    function handleReset() {
        props.setSearchQuery("");
    }

    return (
        <div className="search-bar search-bar-row">
            <input
                type="text"
                placeholder="Search by Name or other fields..."
                value={props.searchQuery}
                onChange={(e) => props.setSearchQuery(e.target.value)}
                className="search-input"
            />

            <button
                onClick={handleReset}
                className={`${props.searchQuery ? "button-group-button" : "button-grey-search"}`}
            >
                {translatedInfo["Reset Filters"][props.language]}
            </button>
        </div>
    );
}